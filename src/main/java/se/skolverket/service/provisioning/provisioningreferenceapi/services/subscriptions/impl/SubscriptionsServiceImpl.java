package se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.impl;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.SharedData;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.web.client.WebClient;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.ResourceType;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.CircuitBreakerFactory;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.SubscriptionsService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.database.SubscriptionsDatabaseService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.model.Subscription;
import se.skolverket.service.provisioning.provisioningreferenceapi.token.GuardianOfTheTokenService;
import se.skolverket.service.provisioning.provisioningreferenceapi.token.helper.TokenHelper;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class SubscriptionsServiceImpl implements SubscriptionsService {

  private final SubscriptionsDatabaseService subscriptionsDatabaseService;
  private final Vertx vertx;

  private final CircuitBreakerFactory circuitBreakerFactory;
  private final WebClient webClient;

  private final SharedData sharedData;

  private final GuardianOfTheTokenService guardianOfTheTokenService;

  public SubscriptionsServiceImpl(SubscriptionsDatabaseService subscriptionsDatabaseService,
                                  Vertx vertx,
                                  CircuitBreakerFactory circuitBreakerFactory,
                                  WebClient webClient,
                                  SharedData sharedData,
                                  GuardianOfTheTokenService guardianOfTheTokenService) {
    this.subscriptionsDatabaseService = subscriptionsDatabaseService;
    this.vertx = vertx;
    this.circuitBreakerFactory = circuitBreakerFactory;
    this.webClient = webClient;
    this.sharedData = sharedData;
    this.guardianOfTheTokenService = guardianOfTheTokenService;
  }

  public Future<Subscription> createSubscription(Subscription subscription) {
    return subscriptionsDatabaseService.insertSubscription(subscription);
  }

  public Future<Void> deleteSubscription(String id) {
    return subscriptionsDatabaseService.deleteSubscription(id)
      .compose(v -> Future.succeededFuture());
  }

  /**
   * Calling this interface indicates that *some* service data has been added,
   * modified, or removed. In case data has been removed, the service will NOT
   * fetch subscriptions based on the deleted resource type, but rather ALL
   * subscriptions. This because subscribers are in case of deletion expected
   * to query for the deleted data themselves using the '/deletedEntities'
   * endpoint. Passing a resource type in case of deletion has a purely
   * informational purpose through the resulting log-statement.
   */
  public Future<Void> dataChanged(ResourceType resourceType, boolean deleted) {
    log.info("Data has changed for resource type: {}", resourceType);

    if (deleted) {
      log.info("Deleted flag set, fetching all subscriptions");
      return subscriptionsDatabaseService.getSubscriptions()
        .compose(subscriptions -> {
          log.info("Found {} subscriptions", subscriptions.size());
          return handleDeleted(subscriptions);
        });
    }

    return subscriptionsDatabaseService.getSubscriptionsOf(resourceType)
      .compose(subscriptions -> {
        log.info("Found {} subscriptions", subscriptions.size());
        return handleModified(subscriptions, resourceType);
      });
  }

  private Future<Void> handleDeleted(List<Subscription> subscriptions) {
    JsonObject body = new JsonObject().put("deletedEntities", true);
    return handleSubscriptions(subscriptions, body);
  }

  private Future<Void> handleModified(List<Subscription> subscriptions, ResourceType resourceType) {
    JsonObject body = new JsonObject().put("modifiedEntities", new JsonArray().add(resourceType.toString()));
    return handleSubscriptions(subscriptions, body);
  }

  private Future<Void> handleSubscriptions(List<Subscription> subscriptions, JsonObject body) {

    List<Future<Void>> futureList = subscriptions.stream().map(subscription -> {
      log.info("Handling subscription with id: {} name '{}'", subscription.getId(), subscription.getName());
      return circuitBreakerFactory.getCircuitBreaker(vertx, subscription.getId())
        .<Void>execute(promise ->
          TokenHelper.getToken(sharedData, guardianOfTheTokenService)
            .compose(token -> webClient.postAbs(subscription.getTarget())
              .authentication(new TokenCredentials(token))
              .sendJsonObject(body))
            .<Void>compose(resp -> {
              if (resp.statusCode() != 200) {
                log.error("Subscription with id: {} name '{}' failed", subscription.getId(), subscription.getName());
                return Future.failedFuture("Status code wasn't 200");
              }
              log.info("Subscription '{}' succeeded in reaching the target endpoint", subscription.getId());
              return Future.succeededFuture();
            })
            .onFailure(t -> log.info("Subscription '{}':{} failed to reach endpoint: {}. ", subscription.getId(), subscription.toJson().encode(), t.getMessage()))
            .onComplete(promise))
        .onComplete(ar -> {
          if (ar.failed()) {
            log.error("CircuitBreaker: Subscription '{}':{} failed to reach endpoint: {}", subscription.getId(), subscription.toJson().encode(), ar.cause().getMessage(), ar.cause());
            ar.cause().printStackTrace();
          }
        });
    }).collect(Collectors.toList());

    return Future.join(futureList)
      .compose(v -> Future.succeededFuture());
  }


}

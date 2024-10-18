package se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.ProxyIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.SharedData;
import io.vertx.ext.web.client.WebClient;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.ResourceType;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.database.SubscriptionsDatabaseService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.impl.SubscriptionsServiceImpl;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.model.Subscription;
import se.skolverket.service.provisioning.provisioningreferenceapi.token.GuardianOfTheTokenService;

@ProxyGen
@VertxGen
public interface SubscriptionsService {

  String ADDRESS = "subscriptions-service";

  @ProxyIgnore
  @GenIgnore
  static SubscriptionsService create(SubscriptionsDatabaseService subscriptionsDatabaseService,
                                     Vertx vertx,
                                     CircuitBreakerFactory circuitBreakerFactory,
                                     WebClient webClient,
                                     SharedData sharedData,
                                     GuardianOfTheTokenService guardianOfTheTokenService,
                                     Integer subscriptionExpiresIn) {
    return new SubscriptionsServiceImpl(subscriptionsDatabaseService, vertx, circuitBreakerFactory, webClient, sharedData, guardianOfTheTokenService, subscriptionExpiresIn);
  }

  @ProxyIgnore
  @GenIgnore
  static SubscriptionsService createProxy(Vertx vertx) {
    return new SubscriptionsServiceVertxEBProxy(vertx, SubscriptionsService.ADDRESS);
  }

  Future<Subscription> createSubscription(Subscription subscription);

  Future<Subscription> renewSubscription(String id);

  Future<Void> deleteSubscription(String id);

  Future<Void> dataChanged(ResourceType resourceType, boolean deleted);
}

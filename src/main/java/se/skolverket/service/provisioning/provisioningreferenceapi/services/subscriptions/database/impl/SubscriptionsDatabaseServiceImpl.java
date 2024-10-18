package se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.database.impl;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.serviceproxy.ServiceException;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.DatabaseServiceHelper;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.ResourceType;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.database.SubscriptionsDatabaseService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.model.Subscription;

import java.util.List;
import java.util.stream.Collectors;

import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.Constants.BSON_ID;

@Slf4j
public class SubscriptionsDatabaseServiceImpl implements SubscriptionsDatabaseService {

  private final MongoClient mongoClient;

  private static final String COLLECTION_NAME = "subscriptions";

  public SubscriptionsDatabaseServiceImpl(MongoClient mongoClient) {
    this.mongoClient = mongoClient;
  }

  @Override
  public Future<Subscription> insertSubscription(Subscription subscription) {
    return mongoClient.insert(COLLECTION_NAME, subscription.toBson())
      .compose(id -> Future.succeededFuture(subscription))
      .recover(DatabaseServiceHelper::errorHandler);
  }

  @Override
  public Future<Subscription> saveSubscription(Subscription subscription) {
    return mongoClient.save(COLLECTION_NAME, subscription.toBson())
      .compose(id -> Future.succeededFuture(subscription))
      .recover(DatabaseServiceHelper::errorHandler);
  }

  @Override
  public Future<Object> deleteSubscription(String id) {
    log.info("Deleting subscription with ID '{}'", id);
    return mongoClient.removeDocument(COLLECTION_NAME, new JsonObject().put(BSON_ID, id))
      .compose(dr -> Future.succeededFuture())
      .recover(DatabaseServiceHelper::errorHandler);
  }

  @Override
  public Future<List<Subscription>> getSubscriptions() {
    return findSubscriptions(new JsonObject());
  }

  @Override
  public Future<Subscription> getSubscription(String id) {
    return mongoClient.findOne(COLLECTION_NAME, new JsonObject().put(BSON_ID, id), null)
      .compose(json -> {
        if (json == null) {
          return Future.failedFuture(new ServiceException(404, "Subscription not found"));
        } else {
          return Future.succeededFuture(Subscription.fromBson(json));
        }
      });
  }

  @Override
  public Future<List<Subscription>> getSubscriptionsOf(ResourceType resourceType) {
    return findSubscriptions(new JsonObject().put("resourceTypes", resourceType.toString()));
  }

  private Future<List<Subscription>> findSubscriptions(JsonObject query) {
    return mongoClient.find(COLLECTION_NAME, query)
      .compose(jsonObjects -> Future.succeededFuture(jsonObjects.stream().map(Subscription::fromBson).collect(Collectors.toList())))
      .recover(DatabaseServiceHelper::errorHandler);
  }
}

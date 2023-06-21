package se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.database;

import io.vertx.core.Future;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.ResourceType;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.model.Subscription;

import java.util.List;

public interface SubscriptionsDatabaseService {

  Future<Subscription> insertSubscription(Subscription subscription);
  Future<Object> deleteSubscription(String id);
  Future<List<Subscription>> getSubscriptions();
  Future<List<Subscription>> getSubscriptionsOf(ResourceType resourceType);
}

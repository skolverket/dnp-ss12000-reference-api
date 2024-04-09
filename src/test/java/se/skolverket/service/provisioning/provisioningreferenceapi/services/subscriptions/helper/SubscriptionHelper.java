package se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.helper;

import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.ResourceType;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.model.Subscription;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.model.SubscriptionResourceType;

import java.util.List;

public class SubscriptionHelper {

  public static Subscription validSubscription() {
    return validSubscription("valid-subscription");
  }

  public static Subscription validSubscription(String name) {
    return new Subscription("valid-uuid",
      name, "http://www.some-site.se",
      getResourceTypes());
  }

  private static List<SubscriptionResourceType> getResourceTypes() {
    return List.of(new SubscriptionResourceType(ResourceType.ACTIVITY),
      new SubscriptionResourceType(ResourceType.GROUP),
      new SubscriptionResourceType(ResourceType.PERSON),
      new SubscriptionResourceType(ResourceType.DUTY));
  }
}

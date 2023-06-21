package se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.helper;

import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.ResourceType;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.model.Subscription;

import java.util.List;

public class SubscriptionHelper {

  public static Subscription validSubscription() {
    return new Subscription("valid-uuid",
      "valid-subscription", "http://www.some-site.se",
      List.of(ResourceType.ACTIVITY, ResourceType.GROUP, ResourceType.PERSON, ResourceType.DUTY));
  }

  public static Subscription validSubscription(String name) {
    return new Subscription("valid-uuid",
      name, "http://www.some-site.se",
      List.of(ResourceType.ACTIVITY, ResourceType.GROUP, ResourceType.PERSON, ResourceType.DUTY));
  }
}

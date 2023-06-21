package se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.model;

import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.ResourceType;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubscriptionTest {

  @Test
  void testToFromJson() {
    String uuid = UUID.randomUUID().toString();

    Subscription subscription = new Subscription();
    subscription.setId(uuid);
    subscription.setName("my-subscription");
    subscription.setTarget("www.some-site.se");
    subscription.setResourceTypes(List.of(ResourceType.ACTIVITY, ResourceType.GROUP));

    JsonObject jsonObject = subscription.toJson();
    Subscription parsedSubscription = new Subscription(jsonObject);
    assertEquals(uuid, parsedSubscription.getId());
    assertEquals("my-subscription", parsedSubscription.getName());
    assertEquals("www.some-site.se", parsedSubscription.getTarget());
    assertTrue(parsedSubscription.getResourceTypes().contains(ResourceType.GROUP));
    assertTrue(parsedSubscription.getResourceTypes().contains(ResourceType.ACTIVITY));
    assertEquals(2, parsedSubscription.getResourceTypes().size());
  }

  @Test
  void testToFromBson() {
    String uuid = UUID.randomUUID().toString();

    Subscription subscription = new Subscription(
      uuid, "my-subscription",
      "www.some-site.se", List.of(ResourceType.GROUP, ResourceType.DUTY)
    );

    JsonObject bson = subscription.toBson();
    assertEquals(uuid, bson.getString("_id"));
    assertEquals("does-not-exist", bson.getString("id", "does-not-exist"));
    assertEquals("my-subscription", bson.getString("name"));
    assertEquals("www.some-site.se", bson.getString("target"));
    assertTrue(bson.getJsonArray("resourceTypes").contains("Group"));
    assertTrue(bson.getJsonArray("resourceTypes").contains("Duty"));

    Subscription parsedSubscription = Subscription.fromBson(bson);
    assertEquals(uuid, parsedSubscription.getId());
    assertEquals("my-subscription", parsedSubscription.getName());
    assertEquals("www.some-site.se", parsedSubscription.getTarget());
    assertTrue(parsedSubscription.getResourceTypes().contains(ResourceType.GROUP));
    assertTrue(parsedSubscription.getResourceTypes().contains(ResourceType.DUTY));
    assertEquals(2, parsedSubscription.getResourceTypes().size());
  }
}

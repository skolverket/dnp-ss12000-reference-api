package se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.database.impl;

import com.noenv.wiremongo.WireMongo;
import io.vertx.core.Vertx;
import io.vertx.ext.mongo.MongoClientDeleteResult;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import se.skolverket.service.provisioning.provisioningreferenceapi.ProvisioningReferenceApiAbstractTest;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.ResourceType;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.database.SubscriptionsDatabaseService;

import java.util.List;

import static se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.helper.SubscriptionHelper.validSubscription;

@ExtendWith(VertxExtension.class)
public class SubscriptionsDatabaseServiceImplTest extends ProvisioningReferenceApiAbstractTest {

  private SubscriptionsDatabaseService subscriptionsDatabaseService;
  private WireMongo wireMongo;

  @BeforeEach
  void setup(Vertx vertx, VertxTestContext vertxTestContext) {
    wireMongo = new WireMongo();
    subscriptionsDatabaseService = new SubscriptionsDatabaseServiceImpl(wireMongo.getClient());
    vertxTestContext.completeNow();
  }

  @Test
  void insertSubscription(Vertx vertx, VertxTestContext vertxTestContext) {
    wireMongo.insert().returns("id");

    subscriptionsDatabaseService.insertSubscription(validSubscription())
      .onSuccess(id -> vertxTestContext.completeNow())
      .onFailure(t -> vertxTestContext.failNow("Insert should have succeeded"));
  }

  @Test
  void deleteSubscription(Vertx vertx, VertxTestContext vertxTestContext) {
    wireMongo.removeDocument().returns(new MongoClientDeleteResult());

    subscriptionsDatabaseService.deleteSubscription("id")
      .onSuccess(v -> vertxTestContext.completeNow())
      .onFailure(t -> vertxTestContext.failNow("Delete should have succeeded"));
  }

  @Test
  void getSubscriptions(Vertx vertx, VertxTestContext vertxTestContext) {
    wireMongo.find().returns(List.of(validSubscription().toBson()));

    subscriptionsDatabaseService.getSubscriptions()
      .onSuccess(v -> vertxTestContext.completeNow())
      .onFailure(t -> vertxTestContext.failNow("Delete should have succeeded"));
  }

  @Test
  void getSubscriptionsOf(Vertx vertx, VertxTestContext vertxTestContext) {
    wireMongo.find().returns(List.of(validSubscription().toBson()));

    subscriptionsDatabaseService.getSubscriptionsOf(ResourceType.ACTIVITY)
      .onSuccess(v -> vertxTestContext.completeNow())
      .onFailure(t -> vertxTestContext.failNow("Delete should have succeeded"));
  }
}

package se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxTestContext;
import io.vertx.servicediscovery.ServiceDiscovery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.skolverket.service.provisioning.provisioningreferenceapi.ProvisioningReferenceApiAbstractTest;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.helper.SubscriptionHelper.validSubscription;


class SubscriptionsServiceVerticleTest extends ProvisioningReferenceApiAbstractTest {

  private ServiceDiscovery serviceDiscovery;
  private Integer port;

  @BeforeEach
  @DisplayName("Deploy a verticle")
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    serviceDiscovery = ServiceDiscovery.create(vertx);
    System.err.println("Got Service discovery.");
    port = findRandomOpenPortOnAllLocalInterfaces();
    vertx.deployVerticle(SubscriptionsServiceVerticle.class.getName(), new DeploymentOptions().setConfig(new JsonObject().put("port", port)))
      .onComplete(testContext.succeedingThenComplete());
  }

  @Test
  @DisplayName("Registered to cluster.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void getRecord(VertxTestContext testContext) {
    serviceDiscovery.getRecord(new JsonObject().put("name", SubscriptionsServiceVerticle.SERVICE_NAME).put("expose", true), testContext.succeeding(record -> testContext.verify(() -> {
      assertNotNull(record);
      assertEquals(SubscriptionsServiceVerticle.SERVICE_NAME, record.getName());
      testContext.completeNow();
    })));
  }

  @SuppressWarnings("SpellCheckingInspection")
  @Test
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void badRequestMalformedTargetUrl(Vertx vertx, VertxTestContext vertxTestContext) {
    JsonObject requestBody = validSubscription().toJson();
    requestBody.put("id", UUID.randomUUID().toString());
    requestBody.put("target", "håttp://www.httpbin.org");
    WebClient.create(vertx)
      .post(port, "localhost", "/")
      .sendJsonObject(requestBody)
      .onComplete(
        vertxTestContext.succeeding(bufferHttpResponse -> vertxTestContext.verify(() -> {
          assertEquals(400, bufferHttpResponse.statusCode());
          vertxTestContext.completeNow();
        }))
      );
  }
}

package se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities;


import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxTestContext;
import io.vertx.servicediscovery.ServiceDiscovery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.skolverket.service.provisioning.provisioningreferenceapi.ProvisioningReferenceApiAbstractTest;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;


class DeletedEntriesVerticleTest extends ProvisioningReferenceApiAbstractTest {

  private ServiceDiscovery serviceDiscovery;

  @BeforeEach
  @DisplayName("Deploy a verticle")
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    serviceDiscovery = ServiceDiscovery.create(vertx);
    System.err.println("Got Service discovery.");
    Integer port = findRandomOpenPortOnAllLocalInterfaces();
    vertx.deployVerticle(DeletedEntitiesVerticle.class.getName(), new DeploymentOptions().setConfig(new JsonObject().put("port", port)))
      .onComplete(testContext.succeedingThenComplete());
  }

  @Test
  @DisplayName("Registered to cluster.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void getRecord(VertxTestContext testContext) {
    serviceDiscovery.getRecord(new JsonObject().put("name", DeletedEntitiesVerticle.DELETED_ENTITIES_SERVICE_NAME).put("expose", true), testContext.succeeding(record -> testContext.verify(() -> {
      assertNotNull(record);
      assertEquals(DeletedEntitiesVerticle.DELETED_ENTITIES_SERVICE_NAME, record.getName());
      testContext.completeNow();
    })));
  }
}

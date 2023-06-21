package se.skolverket.service.provisioning.provisioningreferenceapi.services.logging;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static se.skolverket.service.provisioning.provisioningreferenceapi.services.logging.LoggingServiceVerticle.LOGGING_SERVICE_NAME;

public class LoggingServiceVerticleTest extends ProvisioningReferenceApiAbstractTest {

  private ServiceDiscovery serviceDiscovery;

  @BeforeEach
  @DisplayName("Deploy LoggingServiceVerticle")
  void deployVerticle(Vertx vertx, VertxTestContext vertxTestContext) {
    serviceDiscovery = ServiceDiscovery.create(vertx);
    Integer port = findRandomOpenPortOnAllLocalInterfaces();
    vertx.deployVerticle(LoggingServiceVerticle.class.getName(), new DeploymentOptions().setConfig(new JsonObject().put("port", port)))
      .onComplete(vertxTestContext.succeedingThenComplete());
  }

  @Test
  @DisplayName("Registered to cluster.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void getRecord(VertxTestContext testContext) {
    serviceDiscovery.getRecord(new JsonObject()
      .put("name", LOGGING_SERVICE_NAME)
      .put("ingest", true), testContext.succeeding(record -> testContext.verify(() -> {
        assertNotNull(record);
        assertEquals(LOGGING_SERVICE_NAME, record.getName());
        testContext.completeNow();
      })));
  }
}

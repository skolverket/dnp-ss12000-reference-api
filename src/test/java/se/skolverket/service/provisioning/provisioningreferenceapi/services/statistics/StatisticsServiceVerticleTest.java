package se.skolverket.service.provisioning.provisioningreferenceapi.services.statistics;

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
import static se.skolverket.service.provisioning.provisioningreferenceapi.services.statistics.StatisticsServiceVerticle.SERVICE_NAME;

public class StatisticsServiceVerticleTest extends ProvisioningReferenceApiAbstractTest {

  private ServiceDiscovery serviceDiscovery;

  @BeforeEach
  @DisplayName("Deploy StatisticsServiceVerticle")
  void deployVerticle(Vertx vertx, VertxTestContext vertxTestContext) {
    serviceDiscovery = ServiceDiscovery.create(vertx);
    Integer port = findRandomOpenPortOnAllLocalInterfaces();
    vertx.deployVerticle(StatisticsServiceVerticle.class.getName(), new DeploymentOptions().setConfig(new JsonObject().put("port", port)))
      .onComplete(vertxTestContext.succeedingThenComplete());
  }

  @Test
  @DisplayName("Registered to cluster.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void getRecord(VertxTestContext testContext) {
    serviceDiscovery.getRecord(new JsonObject()
      .put("name", SERVICE_NAME)
      .put("ingest", true), testContext.succeeding(record -> testContext.verify(() -> {
        assertNotNull(record);
        assertEquals(SERVICE_NAME, record.getName());
        testContext.completeNow();
      })));
  }
}

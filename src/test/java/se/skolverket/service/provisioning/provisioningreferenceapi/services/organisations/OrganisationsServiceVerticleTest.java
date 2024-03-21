package se.skolverket.service.provisioning.provisioningreferenceapi.services.organisations;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.servicediscovery.ServiceDiscovery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import se.skolverket.service.provisioning.provisioningreferenceapi.ProvisioningReferenceApiAbstractTest;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static se.skolverket.service.provisioning.provisioningreferenceapi.services.organisations.helper.OrganisationsTestData.createInvalidOrganisation;

@ExtendWith(VertxExtension.class)
class OrganisationsServiceVerticleTest extends ProvisioningReferenceApiAbstractTest {

  private ServiceDiscovery serviceDiscovery;

  private Integer port;

  @BeforeEach
  @DisplayName("Deploy OrganisationsServiceVerticle")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    serviceDiscovery = ServiceDiscovery.create(vertx);
    System.out.println("Got Service discovery.");
    port = findRandomOpenPortOnAllLocalInterfaces();
    vertx.deployVerticle(OrganisationsServiceVerticle.class.getName(),
        new DeploymentOptions().setConfig(new JsonObject().put("port", port)))
      .onComplete(testContext.succeedingThenComplete());
  }

  @Test
  @DisplayName("Registered to cluster.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void getRecord(VertxTestContext testContext) {
    serviceDiscovery.getRecord(new JsonObject()
        .put("name", OrganisationsServiceVerticle.SERVICE_NAME)
        .put("ingest", true),
      testContext.succeeding(record -> testContext.verify(() -> {
        assertNotNull(record);
        assertEquals(OrganisationsServiceVerticle.SERVICE_NAME, record.getName());
        testContext.completeNow();
      })));
  }

  @Test
  @DisplayName("Validation Post - Bad request. Organisation with no Id property")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void postOrganisationsBadRequestTest(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    client.post(port, "localhost", "/")
      .sendJsonObject(new JsonObject().put("organisations", new JsonArray().add(createInvalidOrganisation())))
      .onComplete(
        testContext.succeeding(bufferHttpResponse ->
          testContext.verify(() -> {
            assertEquals(400, bufferHttpResponse.statusCode());
            testContext.completeNow();
          })
        )
      );
  }

  @Test
  @DisplayName("Validation Put - Bad request. Organisation with no Id property")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void putOrganisationsBadRequestTest(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    client.put(port, "localhost", "/")
      .sendJsonObject(new JsonObject().put("organisations", new JsonArray().add(createInvalidOrganisation())))
      .onComplete(
        testContext.succeeding(bufferHttpResponse ->
          testContext.verify(() -> {
            assertEquals(400, bufferHttpResponse.statusCode());
            testContext.completeNow();
          })
        )
      );
  }
}

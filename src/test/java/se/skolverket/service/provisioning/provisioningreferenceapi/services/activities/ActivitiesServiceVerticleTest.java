package se.skolverket.service.provisioning.provisioningreferenceapi.services.activities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.servicediscovery.ServiceDiscovery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.model.Activity;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.helper.ActivitiesHelper.validActivity;

@ExtendWith(VertxExtension.class)
public class ActivitiesServiceVerticleTest {
  private ServiceDiscovery serviceDiscovery;

  @BeforeEach
  @DisplayName("Deploy a verticle")
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    ObjectMapper mapper = DatabindCodec.mapper();
    mapper.registerModule(new JavaTimeModule());

    serviceDiscovery = ServiceDiscovery.create(vertx);
    System.out.println("Got service discovery");

    vertx.deployVerticle(
      ActivitiesServiceVerticle.class.getName(),
        new DeploymentOptions().setConfig(new JsonObject().put("port", 30000))
    ).onComplete(testContext.succeedingThenComplete());
  }

  @Test
  @DisplayName("Registered to cluster and receiving http.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void getRecord(VertxTestContext testContext) {
    serviceDiscovery.getRecord(new JsonObject().put("name", ActivitiesServiceVerticle.SERVICE_NAME).put("ingest", true), testContext.succeeding(record -> testContext.verify(() -> {
      assertNotNull(record);
      assertEquals(ActivitiesServiceVerticle.SERVICE_NAME, record.getName());
      testContext.completeNow();
    })));
  }

  @Test
  @DisplayName("POST an activity that is missing a required field")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void postActivitiesMissingRequired(Vertx vertx, VertxTestContext testContext) {
    Activity missingId = validActivity();
    missingId.setId(null);

    vertx.createHttpClient().request(HttpMethod.POST, 30000, "localhost", "/")
      .compose(req ->
        req.send(makeBody(List.of(missingId)))
          .onSuccess(resp -> {
            assertEquals(400, resp.statusCode());
            testContext.completeNow();
          })
          .onFailure(testContext::failNow)
      );
  }

  private Buffer makeBody(List<Activity> activities) {
    JsonObject jsonObject = new JsonObject();
    JsonArray jsonActivities = new JsonArray();
    activities.forEach(activity -> jsonActivities.add(activity.toJson()));
    jsonObject.put("activities", jsonActivities);

    return jsonObject.toBuffer();
  }
}

package se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.handler;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import se.skolverket.service.provisioning.provisioningreferenceapi.helper.AbstractRestApiHelper;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.ActivitiesService;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.helper.ActivitiesHelper.validActivity;

class ActivitiesHandlerTest extends AbstractRestApiHelper {

  private ActivitiesService activitiesService;

  private static final String BASE_URI = "/activities";

  @BeforeEach
  @DisplayName("ActivitiesHandlerTest setup.")
  void setup(Vertx vertx, VertxTestContext testContext) {
    activitiesService = Mockito.mock(ActivitiesService.class);
    testContext.completeNow();
  }

  @Test
  @DisplayName("Get activities to handler.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void getActivities(Vertx vertx, VertxTestContext testContext) {
    Mockito.when(activitiesService.findActivities(any())).thenReturn(Future.succeededFuture(List.of(validActivity())));

    mockServer(vertx, HttpMethod.GET, BASE_URI, ActivitiesHandler.getActivities(activitiesService), testContext)
      .onComplete(testContext.succeeding(port -> {
        WebClient client = WebClient.create(vertx);
        client.get(port, "localhost", BASE_URI)
          .send()
          .onComplete(
            testContext.succeeding(bufferHttpResponse ->
              testContext.verify(() -> {
                assertEquals(200, bufferHttpResponse.statusCode());
                testContext.completeNow();
              })
            )
          );
      }));
  }

  @Test
  @DisplayName("Post activities to handler.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void postActivities(Vertx vertx, VertxTestContext testContext) {
    Mockito.when(activitiesService.createActivities(any())).thenReturn(Future.succeededFuture(List.of(UUID.randomUUID().toString())));
    mockServer(vertx, HttpMethod.POST, BASE_URI, ActivitiesHandler.postActivities(activitiesService), testContext)
      .onComplete(testContext.succeeding(port -> {
        WebClient client = WebClient.create(vertx);
        client.post(port, "localhost", BASE_URI)
          .sendJsonObject(new JsonObject().put("activities", new JsonArray().add(validActivity().toJson())))
          .onComplete(
            testContext.succeeding(bufferHttpResponse ->
              testContext.verify(() -> {
                assertEquals(201, bufferHttpResponse.statusCode());
                testContext.completeNow();
              })
            )
          );
      }));
  }

  @Test
  @DisplayName("Delete activities.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void deleteActivities(Vertx vertx, VertxTestContext testContext) {
    Mockito.when(activitiesService.deleteActivities(any())).thenReturn(Future.succeededFuture());

    mockServer(vertx, HttpMethod.POST, BASE_URI, ActivitiesHandler.deleteActivities(activitiesService), testContext)
      .onComplete(testContext.succeeding(port -> {
        WebClient client = WebClient.create(vertx);
        client.post(port, "localhost", BASE_URI)
          .sendJsonObject(new JsonObject().put("activities", new JsonArray().add(validActivity().toJson())))
          .onComplete(
            testContext.succeeding(bufferHttpResponse ->
              testContext.verify(() -> {
                assertEquals(202, bufferHttpResponse.statusCode());
                testContext.completeNow();
              })
            )
          );
      }));
  }

  @Test
  @DisplayName("Put activities handler.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void putActivitiesSuccessTest(Vertx vertx, VertxTestContext testContext) {
    Mockito.when(activitiesService.updateActivities(any()))
      .thenReturn(Future.succeededFuture(List.of(UUID.randomUUID().toString())));
    mockServer(vertx, HttpMethod.PUT, BASE_URI, ActivitiesHandler.putActivities(activitiesService), testContext)
      .onComplete(testContext.succeeding(ar -> {
        WebClient webClient = WebClient.create(vertx);
        webClient.put(ar, "localhost", BASE_URI)
          .sendJsonObject(new JsonObject().put("activities", new JsonArray().add(validActivity().toJson())))
          .onComplete(
            testContext.succeeding(response -> testContext.verify(() -> {
              assertEquals(201, response.statusCode());
              testContext.completeNow();
            })));
      }));
  }
}

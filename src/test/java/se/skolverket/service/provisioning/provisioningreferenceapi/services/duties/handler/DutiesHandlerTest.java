package se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.handler;

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
import se.skolverket.service.provisioning.provisioningreferenceapi.common.StreamingService;
import se.skolverket.service.provisioning.provisioningreferenceapi.helper.AbstractRestApiHelper;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.handler.ActivitiesHandler;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.DutiesService;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.helper.ActivitiesHelper.validActivity;
import static se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.testdata.DutiesTestData.createValidDuty;

class DutiesHandlerTest extends AbstractRestApiHelper  {
  private static final String URI = "/duties";

  private DutiesService dutiesService;

  @BeforeEach
  @DisplayName("DutiesHandlerTest setup.")
  void setup(VertxTestContext testContext) {
    dutiesService = Mockito.mock(DutiesService.class);
    testContext.completeNow();
  }

  @Test
  @DisplayName("Post duties to handler.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void postDutiesSuccessTest(Vertx vertx, VertxTestContext testContext) {

    Mockito.when(dutiesService.createDuties(any()))
      .thenReturn(Future.succeededFuture(List.of(UUID.randomUUID().toString())));

    mockServer(vertx, HttpMethod.POST, URI, DutiesHandler.postDuties(dutiesService), testContext)
      .onComplete(testContext.succeeding(ar -> {
        WebClient client = WebClient.create(vertx);
        client.post(ar, "localhost", URI)
          .sendJsonObject(new JsonObject().put("duties", new JsonArray().add(createValidDuty().toJson())))
          .onComplete(
            testContext.succeeding(response -> testContext.verify(() -> {
              assertEquals(201, response.statusCode());
              testContext.completeNow();
            })));
      }));
  }

  @Test
  @DisplayName("Put duties to handler.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void putDutiesSuccessTest(Vertx vertx, VertxTestContext testContext) {

    Mockito.when(dutiesService.updateDuties(any()))
      .thenReturn(Future.succeededFuture(List.of(UUID.randomUUID().toString())));

    mockServer(vertx, HttpMethod.PUT, URI, DutiesHandler.putDuties(dutiesService), testContext)
      .onComplete(testContext.succeeding(ar -> {
        WebClient client = WebClient.create(vertx);
        client.put(ar, "localhost", URI)
          .sendJsonObject(new JsonObject().put("duties", new JsonArray().add(createValidDuty().toJson())))
          .onComplete(
            testContext.succeeding(response -> testContext.verify(() -> {
              assertEquals(201, response.statusCode());
              testContext.completeNow();
            })));
      }));
  }

  @Test
  @DisplayName("Delete duties.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void deleteDutiesSuccessTest(Vertx vertx, VertxTestContext testContext) {
    Mockito.when(dutiesService.deleteDuties(any())).thenReturn(Future.succeededFuture());

    mockServer(vertx, HttpMethod.POST, URI, DutiesHandler.deleteDuties(dutiesService), testContext)
      .onComplete(testContext.succeeding(ar -> {
        WebClient client = WebClient.create(vertx);
        client.post(ar, "localhost", URI)
          .sendJsonObject(new JsonObject().put("duties", new JsonArray().add(createValidDuty().toJson())))
          .onComplete(
            testContext.succeeding(response -> testContext.verify(() -> {
              assertEquals(202, response.statusCode());
              testContext.completeNow();
            })));
      }));
  }

  @Test
  @DisplayName("GET duties")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void getDutiesSuccessTest(Vertx vertx, VertxTestContext testContext) {
    StreamingService streamingService = Mockito.mock(StreamingService.class);
    Mockito.when(streamingService.getStream(any(), any())).thenReturn(Future.succeededFuture());

    mockServer(vertx, HttpMethod.GET, URI, DutiesHandler.getDuties(streamingService), testContext)
      .onComplete(testContext.succeeding(port -> {
        WebClient client = WebClient.create(vertx);
        client.get(port, "localhost", URI)
          .send()
          .onComplete(
            testContext.succeeding(bufferHttpResponse ->
              testContext.verify(() -> {
                assertEquals(200, bufferHttpResponse.statusCode());
                testContext.completeNow();
              }))
          );
      }));
  }

  @Test
  @DisplayName("Get one duty handler.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void getDuty (Vertx vertx, VertxTestContext testContext) {
    Mockito.when(dutiesService.getDutiesByDutyIds(any())).thenReturn(Future.succeededFuture(List.of(createValidDuty())));
    mockServer(vertx, HttpMethod.GET, URI + "/:id", DutiesHandler.getDutiesByDutyIds(dutiesService), testContext)
      .onComplete(testContext.succeeding(port -> {
        WebClient client = WebClient.create(vertx);
        client.get(port, "localhost", URI + "/1234567890")
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
}

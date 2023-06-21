package se.skolverket.service.provisioning.provisioningreferenceapi.services.statistics.handler;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import se.skolverket.service.provisioning.provisioningreferenceapi.helper.AbstractRestApiHelper;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.statistics.StatisticsService;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static se.skolverket.service.provisioning.provisioningreferenceapi.services.statistics.helper.StatisticsEntryHelper.validStatisticsEntry;

class StatisticsHandlerTest extends AbstractRestApiHelper {
  private static final String BASE_URI = "/statistics";
  private StatisticsService mockService;

  @BeforeEach
  @DisplayName("StatisticsHandlerTest setup.")
  void setup(VertxTestContext testContext) {
    mockService = Mockito.mock(StatisticsService.class);
    testContext.completeNow();
  }

  @Test
  @DisplayName("Post StatisticsEntry to handler.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void postStatisticsEntry(Vertx vertx, VertxTestContext testContext) {
    Mockito.when(mockService.createStatisticsEntry(any())).thenReturn(Future.succeededFuture(UUID.randomUUID().toString()));

    mockServer(vertx, HttpMethod.POST, BASE_URI, StatisticsHandler.postStatisticsEntry(mockService), testContext)
      .onComplete(testContext.succeeding(port -> {
        WebClient client = WebClient.create(vertx);
        client.post(port, "localhost", BASE_URI)
          .sendJsonObject(new JsonObject(validStatisticsEntry().toJson().toString()))
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
  @DisplayName("Get StatisticsEntry from handler.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void getLogs(Vertx vertx, VertxTestContext testContext) {
    Mockito.when(mockService.getStatisticsEntries(any())).thenReturn(Future.succeededFuture(List.of(validStatisticsEntry())));

    mockServer(vertx, HttpMethod.GET, BASE_URI, StatisticsHandler.getStatistics(mockService), testContext)
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
}

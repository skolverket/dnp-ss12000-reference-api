package se.skolverket.service.provisioning.provisioningreferenceapi.services.logging.handler;

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
import se.skolverket.service.provisioning.provisioningreferenceapi.services.logging.LoggingService;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static se.skolverket.service.provisioning.provisioningreferenceapi.services.logging.helper.LogHelper.validLog;

class LoggingHandlerTest extends AbstractRestApiHelper {
  private LoggingService mockLoggingService;
  private static final String BASE_URI = "/log";

  @BeforeEach
  @DisplayName("LoggingHandlerTest setup.")
  void setup(VertxTestContext testContext) {
    mockLoggingService = Mockito.mock(LoggingService.class);
    testContext.completeNow();
  }

  @Test
  @DisplayName("Post Log to handler.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void postLog(Vertx vertx, VertxTestContext testContext) {
    Mockito.when(mockLoggingService.createLog(any())).thenReturn(Future.succeededFuture(UUID.randomUUID().toString()));

    mockServer(vertx, HttpMethod.POST, BASE_URI, LoggingHandler.postLog(mockLoggingService), testContext)
      .onComplete(testContext.succeeding(port -> {
        WebClient client = WebClient.create(vertx);
        client.post(port, "localhost", BASE_URI)
          .sendJsonObject(new JsonObject(validLog().toJson().toString()))
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
  @DisplayName("Get logs from handler.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void getLogs(Vertx vertx, VertxTestContext testContext) {
    Mockito.when(mockLoggingService.getLogs(any())).thenReturn(Future.succeededFuture(List.of(validLog())));

    mockServer(vertx, HttpMethod.GET, BASE_URI, LoggingHandler.getLogs(mockLoggingService), testContext)
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

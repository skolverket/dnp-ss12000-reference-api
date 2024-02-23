package se.skolverket.service.provisioning.provisioningreferenceapi.services.logging.impl;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.logging.database.LoggingDatabaseService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static se.skolverket.service.provisioning.provisioningreferenceapi.services.logging.helper.LogHelper.validLog;

@ExtendWith(VertxExtension.class)
class LoggingServiceImplTest {
  private LoggingDatabaseService mockLoggingDatabaseService;
  private LoggingServiceImpl loggingService;

  @BeforeEach
  @DisplayName("LoggingServiceImpl setup.")
  void setup(VertxTestContext testContext) {
    mockLoggingDatabaseService = Mockito.mock(LoggingDatabaseService.class);
    loggingService = new LoggingServiceImpl(mockLoggingDatabaseService);
    testContext.completeNow();
  }

  @Test
  @DisplayName("Create LogEntry")
  void createLogSuccess(VertxTestContext testContext) {
    Mockito.when(mockLoggingDatabaseService.insertLog(any())).thenReturn(Future.succeededFuture("id"));

    loggingService.createLog(validLog())
      .onComplete(testContext.succeeding(strings -> testContext.verify(() -> {
        Mockito.verify(mockLoggingDatabaseService, Mockito.times(1)).insertLog(any());
        testContext.completeNow();
      })));
  }

  @Test
  @DisplayName("getLogs")
  void getLogs(VertxTestContext testContext) {
    Mockito.when(mockLoggingDatabaseService.findLogs(any())).thenReturn(Future.succeededFuture(List.of(validLog())));

    loggingService.getLogs(new JsonObject())
      .onComplete(testContext.succeeding(strings -> testContext.verify(() -> {
        Mockito.verify(mockLoggingDatabaseService, Mockito.times(1)).findLogs(any());
        testContext.completeNow();
      })));
  }
}

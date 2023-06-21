package se.skolverket.service.provisioning.provisioningreferenceapi.services.logging.database.impl;

import com.noenv.wiremongo.WireMongo;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import se.skolverket.service.provisioning.provisioningreferenceapi.ProvisioningReferenceApiAbstractTest;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.logging.database.LoggingDatabaseService;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.skolverket.service.provisioning.provisioningreferenceapi.services.logging.helper.LogHelper.validLog;

@ExtendWith(VertxExtension.class)
class LoggingDatabaseServiceImplIntegrationTest extends ProvisioningReferenceApiAbstractTest {

  private WireMongo wireMongo;
  private LoggingDatabaseService loggingDatabaseService;

  @BeforeEach
  @DisplayName("Mock logging database")
  void setup(VertxTestContext testContext) {
    wireMongo = new WireMongo();
    loggingDatabaseService = new LoggingDatabaseServiceImpl(wireMongo.getClient());
    testContext.completeNow();
  }

  @Test
  @DisplayName("Insert log to database.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void insertLog(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);
    wireMongo.insert()
      .stub(() -> {
        checkpoint.flag();
        return UUID.randomUUID().toString();
      });
    loggingDatabaseService
      .insertLog(validLog()).onComplete(testContext.succeeding(string -> testContext.verify(() -> {
        assertNotNull(string);
        checkpoint.flag();
      })));
  }

  @Test
  @DisplayName("Get Logs from database with success.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void getLogs(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);
    wireMongo.findWithOptions()
      .stub(() -> {
        checkpoint.flag();
        return List.of(new JsonObject());
      });
    JsonObject queryOptions = new JsonObject()
      .put("req", new JsonObject())
      .put("cursor", new JsonObject().put("limit", -1));
    loggingDatabaseService
      .findLogs(
        queryOptions
      ).onComplete(testContext.succeeding(jsonObjectList -> testContext.verify(() -> {
        assertTrue(jsonObjectList.size() > 0);
        checkpoint.flag();
      })));
  }
}

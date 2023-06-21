package se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.database;

import com.noenv.wiremongo.WireMongo;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClientBulkWriteResult;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.skolverket.service.provisioning.provisioningreferenceapi.ProvisioningReferenceApiAbstractTest;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.database.impl.DutiesDatabaseServiceImpl;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.testdata.DutiesTestData.createValidDuty;
import static se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.testdata.DutiesTestData.createValidDutyBson;
import static se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.helper.PersonHelper.validPerson;

class DutiesDatabaseServiceImplIntegrationTest extends ProvisioningReferenceApiAbstractTest {

  private DutiesDatabaseService databaseService;

  private static WireMongo wireMongo;

  @BeforeEach
  @DisplayName("Mock duties database service")
  void setup(VertxTestContext testContext) {
    wireMongo = new WireMongo();
    databaseService = new DutiesDatabaseServiceImpl(wireMongo.getClient());
    testContext.completeNow();
  }

  @Test
  @DisplayName("Find duties in DB")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void findDutiesSuccessTest(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);
    wireMongo.findWithOptions()
      .stub(() -> {
        checkpoint.flag();
        return List.of(createValidDutyBson());
      });
    JsonObject queryOptions = new JsonObject()
      .put("req", new JsonObject())
      .put("cursor", new JsonObject().put("limit", -1));
    databaseService
      .findDuties(
        queryOptions
      ).onComplete(testContext.succeeding(jsonObjectList -> testContext.verify(() -> {
        assertTrue(jsonObjectList.size() > 0);
        checkpoint.flag();
      })));
  }

  @Test
  @DisplayName("Find duties by duty ids in DB")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void findDutiesByDutyIdSuccessTest(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);
    wireMongo.find()
      .stub(() -> {
        checkpoint.flag();
        return List.of(createValidDutyBson());
      });
    databaseService
      .findDutiesByDutyIds(List.of("1")).onComplete(testContext.succeeding(jsonObjectList -> testContext.verify(() -> {
        assertTrue(jsonObjectList.size() > 0);
        checkpoint.flag();
      })));
  }

  @Test
  @DisplayName("Insert duties to database with success.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void insertDutiesSuccessTest(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);
    wireMongo.find()
      .stub(() -> new LinkedList<>());
    wireMongo.bulkWrite()
      .stub(() -> {
        checkpoint.flag();
        return new MongoClientBulkWriteResult();
      });
    databaseService.insertDuties(List.of(createValidDuty(), createValidDuty()))
      .onComplete(testContext.succeeding(strings -> {
        assertEquals(2, strings.size());
        checkpoint.flag();
      }));
  }

  @Test
  @DisplayName("Insert duties to database with success.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void removeDocumentsDutiesSuccessTest(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);
    wireMongo.find()
      .returns(List.of(new JsonObject().put("_id", 1), new JsonObject().put("_id", 2)));
    wireMongo.removeDocuments()
      .stub(() -> {
        checkpoint.flag();
        return null;
      });
    databaseService.deleteDuties(List.of(createValidDuty(), createValidDuty()))
      .onComplete(testContext.succeeding(deletedIds -> {
        assertEquals(2, deletedIds.size());
        checkpoint.flag();
      }));
  }

  @Test
  @DisplayName("Update duties in database with success.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void saveDutiesSuccessTest(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(3);
    wireMongo.findOne()
      .stub(() -> validPerson().toJson().put("meta", new JsonObject()
        .put("created", new JsonObject().put("$date", "2022-09-26T00:00:00Z"))));
    wireMongo.save()
      .stub(() -> {
        checkpoint.flag();
        return UUID.randomUUID().toString();
      });
    databaseService.saveDuties(List.of(createValidDuty(), createValidDuty()))
      .onComplete(testContext.succeeding(strings -> {
        assertEquals(2, strings.size());
        checkpoint.flag();
      }));
  }

  @Test
  @DisplayName("Partial duty update failure.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void saveDutiesPartialFailureTest(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(1);
    wireMongo.save()
      .returns("abc123")
      .returnsConnectionException();
    databaseService.saveDuties(List.of(createValidDuty(), createValidDuty()))
      .onFailure(t -> {
        // t should be an instance of ServiceException
        checkpoint.flag();
      });
  }

}

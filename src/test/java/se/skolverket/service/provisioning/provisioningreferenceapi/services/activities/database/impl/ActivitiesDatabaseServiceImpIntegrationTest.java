package se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.database.impl;

import com.mongodb.MongoWriteException;
import com.mongodb.ServerAddress;
import com.mongodb.bulk.BulkWriteError;
import com.noenv.wiremongo.WireMongo;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClientBulkWriteResult;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxTestContext;
import io.vertx.serviceproxy.ServiceException;
import org.bson.BsonDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.skolverket.service.provisioning.provisioningreferenceapi.ProvisioningReferenceApiAbstractTest;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.database.ActivitiesDatabaseService;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.helper.ActivitiesHelper.validActivity;
import static se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.helper.ActivitiesHelper.validActivityBson;
import static se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.helper.PersonHelper.validPerson;

class ActivitiesDatabaseServiceImpIntegrationTest extends ProvisioningReferenceApiAbstractTest {

  private ActivitiesDatabaseService activitiesDatabaseService;
  private WireMongo wireMongo;

  @BeforeEach
  @DisplayName("Mock activities database")
  void setup(VertxTestContext testContext) {
    wireMongo = new WireMongo();
    activitiesDatabaseService = new ActivitiesDatabaseServiceImpl(wireMongo.getClient());
    testContext.completeNow();
  }

  @Test
  @DisplayName("Find activities in DB")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void findActivitiesSuccessTest(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);
    wireMongo.findWithOptions()
      .stub(() -> {
        checkpoint.flag();
        return List.of(validActivityBson(vertx));
      });
    JsonObject queryOptions = new JsonObject()
      .put("req", new JsonObject())
      .put("cursor", new JsonObject().put("limit", -1));
    activitiesDatabaseService
      .findActivities(
        queryOptions
      ).onComplete(testContext.succeeding(jsonObjectList -> testContext.verify(() -> {
        assertTrue(jsonObjectList.size() > 0);
        checkpoint.flag();
      })));
  }

  @Test
  @DisplayName("Insert activity to database.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void insertActivities(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);
    wireMongo.findOne()
      .stub(() -> validPerson().toJson().put("meta", new JsonObject()
        .put("created", new JsonObject().put("$date", "2022-09-26T00:00:00Z"))));
    wireMongo.find()
        .stub(() -> new LinkedList<>());
    wireMongo.bulkWrite()
      .stub(() -> {
        checkpoint.flag();
        return new MongoClientBulkWriteResult();
      });
    activitiesDatabaseService
      .insertActivities(
        List.of(validActivity(), validActivity(), validActivity())
      ).onComplete(testContext.succeeding(strings -> testContext.verify(() -> {
        assertTrue(strings.size() > 0);
        checkpoint.flag();
      })));
  }

  @Test
  @DisplayName("Insert activity to database - Error 11000 in DB insert.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void insertActivitiesError11000Insert(VertxTestContext testContext) {
    wireMongo.find()
      .stub(() -> List.of());
    wireMongo.bulkWrite().returnsError(new MongoWriteException(new BulkWriteError(11000, "Test error: { _id: \"TEST-ID\" }", new BsonDocument() , 0), new ServerAddress()));
    activitiesDatabaseService.insertActivities(List.of(validActivity()))
      .onComplete(
        testContext.failing(throwable -> testContext.verify(() -> {
          assertEquals(ServiceException.class.getName(), throwable.getClass().getName());
          assertEquals(409, ((ServiceException) throwable).failureCode());
          assertEquals("TEST-ID", ((ServiceException) throwable).getDebugInfo().getJsonArray("id").getString(0));
          testContext.completeNow();
        })));
  }

  @Test
  @DisplayName("Insert activity to database - Error 11000 in DB check.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void insertActivitiesError11000Check(VertxTestContext testContext) {
    wireMongo.find()
      .stub(() -> List.of(validActivity().toBson()));
    activitiesDatabaseService.insertActivities(List.of(validActivity()))
      .onComplete(
        testContext.failing(throwable -> testContext.verify(() -> {
          assertEquals(ServiceException.class.getName(), throwable.getClass().getName());
          assertEquals(409, ((ServiceException) throwable).failureCode());
          testContext.completeNow();
        })));
  }

  @Test
  @DisplayName("Save activities to database with success.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void saveActivitiesSuccessTest(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(3);
    wireMongo.findOne()
      .stub(() -> validPerson().toJson().put("meta", new JsonObject()
        .put("created", new JsonObject().put("$date", "2022-09-26T00:00:00Z"))));
    wireMongo.save().stub(() -> {
      checkpoint.flag();
      return UUID.randomUUID().toString();
    });
    activitiesDatabaseService.saveActivities(List.of(validActivity(), validActivity()))
      .onComplete(testContext.succeeding(ar -> testContext.verify(() -> {
        assertEquals(2, ar.size());
        checkpoint.flag();
      })));
  }

  @Test
  @DisplayName("Delete activities from database")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void deleteActivitiesSuccess(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);
    wireMongo.find()
      .returns(List.of(new JsonObject().put("_id", 1), new JsonObject().put("_id", 2)));
    wireMongo.removeDocuments()
      .stub(() -> {
        checkpoint.flag();
        return null;
      });
    activitiesDatabaseService.deleteActivities(List.of(validActivity(), validActivity()))
      .onComplete(testContext.succeeding(deletedIds -> {
        assertEquals(2, deletedIds.size());
        checkpoint.flag();
      }));
  }


}

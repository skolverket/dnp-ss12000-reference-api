package se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.database.impl;

import com.noenv.wiremongo.WireMongo;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClientBulkWriteResult;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.serviceproxy.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import se.skolverket.service.provisioning.provisioningreferenceapi.ProvisioningReferenceApiAbstractTest;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.database.PersonsDatabaseService;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.helper.PersonHelper.validPerson;

@ExtendWith(VertxExtension.class)
class PersonsDatabaseServiceImpIntegrationTest extends ProvisioningReferenceApiAbstractTest {

  private PersonsDatabaseService personsDatabaseService;
  private WireMongo wireMongo;

  @BeforeEach
  @DisplayName("Mock persons database")
  void setup(VertxTestContext testContext) {
    wireMongo = new WireMongo();
    personsDatabaseService = new PersonsDatabaseServiceImpl(wireMongo.getClient());
    testContext.completeNow();
  }

  @Test
  @DisplayName("Get Persons from database with success.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void getPersons(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);
    wireMongo.findWithOptions()
      .stub(() -> {
        checkpoint.flag();
        return List.of(new JsonObject());
      });
    JsonObject queryOptions = new JsonObject()
      .put("req", new JsonObject())
      .put("cursor", new JsonObject().put("limit", -1));
    personsDatabaseService
      .findPersons(
        queryOptions
      ).onComplete(testContext.succeeding(jsonObjectList -> testContext.verify(() -> {
        assertTrue(jsonObjectList.size() > 0);
        checkpoint.flag();
      })));
  }

  @Test
  @DisplayName("Find Persons by ids from database with success.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void findPersonsByPersonIds(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);
    wireMongo.find()
      .stub(() -> {
        checkpoint.flag();
        return List.of(validPerson().toJson());
      });
    personsDatabaseService
      .findPersonsByPersonIds(List.of("1", "2", "3")).onComplete(testContext.succeeding(jsonObjectList -> testContext.verify(() -> {
        assertTrue(jsonObjectList.size() > 0);
        checkpoint.flag();
      })));
  }

  @Test
  @DisplayName("Saver person to database with success.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void savePersons(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(4);
    wireMongo.findOne()
      .stub(() -> validPerson().toJson().put("meta", new JsonObject()
        .put("created", new JsonObject().put("$date", "2022-09-26T00:00:00Z"))));

    wireMongo.save()
      .stub(() -> {
        checkpoint.flag();
        return UUID.randomUUID().toString();
      });
    personsDatabaseService
      .savePersons(
        List.of(validPerson(), validPerson(), validPerson())
      ).onComplete(testContext.succeeding(strings -> testContext.verify(() -> {
        assertTrue(strings.size() > 0);
        checkpoint.flag();
      })));
  }

  @Test
  @DisplayName("Insert person to database.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void insertPersons(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);
    wireMongo.find()
      .stub(() -> new LinkedList<>());
    wireMongo.bulkWrite()
      .stub(() -> {
        checkpoint.flag();
        return new MongoClientBulkWriteResult();
      });
    personsDatabaseService
      .insertPersons(
        List.of(validPerson(), validPerson(), validPerson())
      ).onComplete(testContext.succeeding(strings -> testContext.verify(() -> {
        assertTrue(strings.size() > 0);
        checkpoint.flag();
      })));
  }

  @Test
  @DisplayName("Insert person to database - Error 11000.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void insertPersonsError11000(VertxTestContext testContext) {
    wireMongo.find()
      .stub(() -> List.of(validPerson().toBson()));
    personsDatabaseService.insertPersons(List.of(validPerson()))
      .onComplete(
        testContext.failing(throwable -> testContext.verify(() -> {
          assertEquals(ServiceException.class.getName(), throwable.getClass().getName());
          assertEquals(409, ((ServiceException) throwable).failureCode());
          testContext.completeNow();
        })));
  }

  @Test
  @DisplayName("Delete persons in database.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void deletePersonsSuccessTest(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);
    wireMongo.find()
      .returns(List.of(new JsonObject().put("_id", 1), new JsonObject().put("_id", 2)));
    wireMongo.removeDocuments()
      .stub(() -> {
        checkpoint.flag();
        return null;
      });
    personsDatabaseService.deletePersons(List.of(validPerson(), validPerson()))
      .onComplete(testContext.succeeding(deletedIds -> {
        assertEquals(2, deletedIds.size());
        checkpoint.flag();
      }));
  }


}

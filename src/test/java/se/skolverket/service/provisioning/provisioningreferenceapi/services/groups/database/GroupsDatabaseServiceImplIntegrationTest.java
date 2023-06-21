package se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.database;

import com.noenv.wiremongo.WireMongo;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClientBulkWriteResult;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxTestContext;
import io.vertx.serviceproxy.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.skolverket.service.provisioning.provisioningreferenceapi.ProvisioningReferenceApiAbstractTest;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.database.impl.GroupsDatabaseServiceImpl;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.testdata.GroupTestData.createValidGroup;


class GroupsDatabaseServiceImplIntegrationTest extends ProvisioningReferenceApiAbstractTest {

  private GroupsDatabaseService databaseService;
  private WireMongo wireMongo;

  @BeforeEach
  @DisplayName("Mock groups database service")
  void setup(VertxTestContext testContext) {
    wireMongo = new WireMongo();
    databaseService = new GroupsDatabaseServiceImpl(wireMongo.getClient());
    testContext.completeNow();
  }

  @Test
  @DisplayName("Get Groups from database with success.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void getGroups(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);
    wireMongo.findWithOptions()
      .stub(() -> {
        checkpoint.flag();
        return List.of(new JsonObject());
      });
    JsonObject queryOptions = new JsonObject()
      .put("req", new JsonObject())
      .put("cursor", new JsonObject().put("limit", -1));
    databaseService
      .findGroups(
        queryOptions
      ).onComplete(testContext.succeeding(jsonObjectList -> testContext.verify(() -> {
        assertTrue(jsonObjectList.size() > 0);
        checkpoint.flag();
      })));
  }

  @Test
  @DisplayName("Get Groups By Group Ids from database with success.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void getGroupsByGroupIds(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);
    wireMongo.find()
      .stub(() -> {
        checkpoint.flag();
        return List.of(new JsonObject());
      });
    databaseService
      .findGroupsByGroupId(List.of("1")).onComplete(testContext.succeeding(jsonObjectList -> testContext.verify(() -> {
        assertTrue(jsonObjectList.size() > 0);
        checkpoint.flag();
      })));
  }

  @Test
  @DisplayName("Save groups to database with success.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void saveGroupsSuccessTest(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(3);
    wireMongo.findOne()
      .stub(() -> createValidGroup().toJson().put("meta", new JsonObject()
        .put("created", new JsonObject().put("$date", "2022-09-26T00:00:00Z"))));

    wireMongo.save().stub(() -> {
      checkpoint.flag();
      return UUID.randomUUID().toString();
    });
    databaseService.saveGroups(List.of(createValidGroup(), createValidGroup()))
      .onComplete(testContext.succeeding(ar -> testContext.verify(() -> {
        assertEquals(2, ar.size());
        checkpoint.flag();
      })));
  }

  @Test
  @DisplayName("Insert groups to database with success.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void insertGroupsSuccessTest(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);
    wireMongo.find()
      .stub(() -> new LinkedList<>());
    wireMongo.bulkWrite()
      .stub(() -> {
        checkpoint.flag();
        return new MongoClientBulkWriteResult();
      });
    databaseService.insertGroups(List.of(createValidGroup(), createValidGroup(), createValidGroup()))
      .onComplete(testContext.succeeding(strings -> {
        assertEquals(3, strings.size());
        checkpoint.flag();
      }));
  }

  @Test
  @DisplayName("Insert group to database - Error 11000.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void insertGroupsError11000(VertxTestContext testContext) {
    wireMongo.find()
      .stub(() -> List.of(createValidGroup().toBson()));
    databaseService.insertGroups(List.of(createValidGroup()))
      .onComplete(
        testContext.failing(throwable -> testContext.verify(() -> {
          assertEquals(ServiceException.class.getName(), throwable.getClass().getName());
          assertEquals(409, ((ServiceException) throwable).failureCode());
          testContext.completeNow();
        })));
  }

  @Test
  @DisplayName("Delete groups in database.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void deleteGroupsSuccessTest(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);
    wireMongo.find()
      .returns(List.of(new JsonObject().put("_id", 1), new JsonObject().put("_id", 2)));
    wireMongo.removeDocuments()
      .stub(() -> {
        checkpoint.flag();
        return null;
      });
    databaseService.deleteGroups(List.of(createValidGroup(), createValidGroup()))
      .onComplete(testContext.succeeding(deletedIds -> {
        assertEquals(2, deletedIds.size());
        checkpoint.flag();
      }));
  }
}

package se.skolverket.service.provisioning.provisioningreferenceapi.services.organisations.database;

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
import se.skolverket.service.provisioning.provisioningreferenceapi.services.organisations.database.impl.OrganisationsDatabaseServiceImpl;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.skolverket.service.provisioning.provisioningreferenceapi.services.organisations.helper.OrganisationsTestData.createValidOrganisation;
import static se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.helper.PersonHelper.validPerson;

class DutiesDatabaseServiceImplIntegrationTest extends ProvisioningReferenceApiAbstractTest {

  private OrganisationsDatabaseService databaseService;

  private static WireMongo wireMongo;

  @BeforeEach
  @DisplayName("Mock organisations database service")
  void setup(VertxTestContext testContext) {
    wireMongo = new WireMongo();
    databaseService = new OrganisationsDatabaseServiceImpl(wireMongo.getClient());
    testContext.completeNow();
  }

  @Test
  @DisplayName("Find organisations in DB")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void findOrganisationsSuccessTest(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);
    wireMongo.findWithOptions()
      .stub(() -> {
        checkpoint.flag();
        return List.of(createValidOrganisation().toBson());
      });
    JsonObject queryOptions = new JsonObject()
      .put("req", new JsonObject())
      .put("cursor", new JsonObject().put("limit", -1));
    databaseService
      .findOrganisations(
        queryOptions
      ).onComplete(testContext.succeeding(jsonObjectList -> testContext.verify(() -> {
        assertTrue(jsonObjectList.size() > 0);
        checkpoint.flag();
      })));
  }

  @Test
  @DisplayName("Insert organisations to database with success.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void insertOrganisationsSuccessTest(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);
    wireMongo.find()
      .stub(() -> new LinkedList<>());
    wireMongo.bulkWrite()
      .stub(() -> {
        checkpoint.flag();
        return new MongoClientBulkWriteResult();
      });
    databaseService.insertOrganisations(List.of(createValidOrganisation(), createValidOrganisation()))
      .onComplete(testContext.succeeding(strings -> {
        assertEquals(2, strings.size());
        checkpoint.flag();
      }));
  }

  @Test
  @DisplayName("Insert organisations to database with success.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void removeDocumentsOrganisationsSuccessTest(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);
    wireMongo.find()
      .returns(List.of(new JsonObject().put("_id", 1), new JsonObject().put("_id", 2)));
    wireMongo.removeDocuments()
      .stub(() -> {
        checkpoint.flag();
        return null;
      });
    databaseService.deleteOrganisations(List.of(createValidOrganisation(), createValidOrganisation()))
      .onComplete(testContext.succeeding(deletedIds -> {
        assertEquals(2, deletedIds.size());
        checkpoint.flag();
      }));
  }

  @Test
  @DisplayName("Update organisations in database with success.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void saveOrganisationsSuccessTest(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(3);
    wireMongo.findOne()
      .stub(() -> validPerson().toJson().put("meta", new JsonObject()
        .put("created", new JsonObject().put("$date", "2022-09-26T00:00:00Z"))));
    wireMongo.save()
      .stub(() -> {
        checkpoint.flag();
        return UUID.randomUUID().toString();
      });
    databaseService.saveOrganisations(List.of(createValidOrganisation(), createValidOrganisation()))
      .onComplete(testContext.succeeding(strings -> {
        assertEquals(2, strings.size());
        checkpoint.flag();
      }));
  }

  @Test
  @DisplayName("Partial organisation update failure.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void saveOrganisationsPartialFailureTest(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(1);
    wireMongo.save()
      .returns("abc123")
      .returnsConnectionException();
    databaseService.saveOrganisations(List.of(createValidOrganisation(), createValidOrganisation()))
      .onFailure(t -> {
        // t should be an instance of ServiceException
        checkpoint.flag();
      });
  }

}

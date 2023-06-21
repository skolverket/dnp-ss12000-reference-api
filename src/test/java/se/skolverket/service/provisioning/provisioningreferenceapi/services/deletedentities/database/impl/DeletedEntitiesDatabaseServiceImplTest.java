package se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.database.impl;

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
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.ResourceType;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.database.DeletedEntitiesDatabaseService;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(VertxExtension.class)
public class DeletedEntitiesDatabaseServiceImplTest extends ProvisioningReferenceApiAbstractTest {

  private DeletedEntitiesDatabaseService deletedEntitiesDatabaseService;
  private WireMongo wireMongo;

  @BeforeEach
  @DisplayName("Mock persons database")
  void setup(VertxTestContext testContext) {
    wireMongo = new WireMongo();
    deletedEntitiesDatabaseService = new DeletedEntitiesDatabaseServiceImpl(wireMongo.getClient());
    testContext.completeNow();
  }

  @Test
  void insertDeletedEntity(VertxTestContext testContext) {
    wireMongo.insert().stub(() -> UUID.randomUUID().toString());

    deletedEntitiesDatabaseService.insertDeletedEntities(List.of("id1", "id2"), ResourceType.PERSON)
      .onSuccess(v -> testContext.completeNow())
      .onFailure(t -> testContext.failNow("Insert should have passed"));
  }

  @Test
  @DisplayName("Get DeletedEntities from database with success.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void getDeletedEntities(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);
    wireMongo.findWithOptions()
      .stub(() -> {
        checkpoint.flag();
        return List.of(new JsonObject()
          .put("_id", "123456789")
          .put("deletedEntityId", "9876543")
          .put("resourceType", "Person"));
      });
    JsonObject queryOptions = new JsonObject()
      .put("req", new JsonObject())
      .put("cursor", new JsonObject().put("limit", -1));
    deletedEntitiesDatabaseService
      .getEntities(
        queryOptions
      ).onComplete(testContext.succeeding(jsonObjectList -> testContext.verify(() -> {
        assertTrue(jsonObjectList.size() > 0);
        checkpoint.flag();
      })));
  }
}

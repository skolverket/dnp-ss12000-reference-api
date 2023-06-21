package se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.impl;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.database.DeletedEntitiesDatabaseService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.model.DeletedEntity;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.SubscriptionsService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;

@ExtendWith(VertxExtension.class)
public class DeletedEntitiesServiceImplTest {

  private DeletedEntitiesDatabaseService mockDeletedEntitiesDatabaseService;
  private DeletedEntitiesServiceImpl deletedEntitiesService;
  private SubscriptionsService subscriptionsService;

  @BeforeEach
  @DisplayName("DeletedEntitiesServiceImplTest setup.")
  void setup(VertxTestContext testContext) {
    mockDeletedEntitiesDatabaseService = Mockito.mock(DeletedEntitiesDatabaseService.class);
    subscriptionsService = Mockito.mock(SubscriptionsService.class);
    deletedEntitiesService = new DeletedEntitiesServiceImpl(mockDeletedEntitiesDatabaseService, subscriptionsService);
    testContext.completeNow();
  }

  @Test
  @DisplayName("getDeletedEntities")
  void getDeletedEntities(VertxTestContext testContext) {
    Mockito.when(mockDeletedEntitiesDatabaseService.getEntities(any())).thenReturn(Future.succeededFuture(List.of(new DeletedEntity())));

    deletedEntitiesService.getEntities(new JsonObject())
      .onComplete(testContext.succeeding(strings -> testContext.verify(() -> {
        Mockito.verify(mockDeletedEntitiesDatabaseService, Mockito.times(1)).getEntities(any());
        testContext.completeNow();
      })));
  }

  @Test
  @DisplayName("entitiesDeleted")
  void entitiesDeleted(VertxTestContext testContext) {
    Mockito.when(mockDeletedEntitiesDatabaseService.insertDeletedEntities(anyList(), any())).thenReturn(Future.succeededFuture());
    Mockito.when(subscriptionsService.dataChanged(any(), anyBoolean())).thenReturn(Future.succeededFuture());

    deletedEntitiesService.entitiesDeleted(anyList(), any())
      .onComplete(testContext.succeeding(strings -> testContext.verify(() -> {
        Mockito.verify(mockDeletedEntitiesDatabaseService, Mockito.times(1)).insertDeletedEntities(anyList(), any());
        Mockito.verify(subscriptionsService, Mockito.times(1)).dataChanged(any(), anyBoolean());
        testContext.completeNow();
      })));
  }
}

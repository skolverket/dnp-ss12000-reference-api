package se.skolverket.service.provisioning.provisioningreferenceapi.services.organisations.impl;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.DeletedEntitiesService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.organisations.OrganisationsService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.organisations.database.OrganisationsDatabaseService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.SubscriptionsService;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static se.skolverket.service.provisioning.provisioningreferenceapi.services.organisations.helper.OrganisationsTestData.createValidOrganisation;

@ExtendWith(VertxExtension.class)
class OrganisationsServiceImplTest {

  private OrganisationsDatabaseService mockOrganisationsDatabaseService;
  private DeletedEntitiesService deletedEntitiesService;
  private SubscriptionsService subscriptionsService;
  private OrganisationsService organisationsService;

  @BeforeEach
  @DisplayName("DutiesServiceImplTest setup")
  void beforeEach(VertxTestContext testContext) {
    mockOrganisationsDatabaseService = Mockito.mock(OrganisationsDatabaseService.class);
    deletedEntitiesService = Mockito.mock(DeletedEntitiesService.class);
    subscriptionsService = Mockito.mock(SubscriptionsService.class);

    organisationsService = new OrganisationsServiceImpl(mockOrganisationsDatabaseService, deletedEntitiesService, subscriptionsService);

    testContext.completeNow();
  }


  @Test
  void createOrganisations(VertxTestContext testContext) {
    Mockito.when(mockOrganisationsDatabaseService.insertOrganisations(anyList()))
      .thenReturn(Future.succeededFuture(List.of(UUID.randomUUID().toString())));
    Mockito.when(subscriptionsService.dataChanged(any(), anyBoolean()))
      .thenReturn(Future.succeededFuture());

    organisationsService.createOrganisations(List.of(createValidOrganisation(), createValidOrganisation()))
      .onComplete(testContext.succeeding(strings -> testContext.verify(() -> {
        Mockito.verify(mockOrganisationsDatabaseService, Mockito.times(1)).insertOrganisations(anyList());
        Mockito.verify(subscriptionsService, Mockito.times(1)).dataChanged(any(), eq(false));
        testContext.completeNow();
      })));
  }

  @Test
  void updateOrganisations(VertxTestContext testContext) {
    Mockito.when(mockOrganisationsDatabaseService.saveOrganisations(any()))
      .thenReturn(Future.succeededFuture(List.of(UUID.randomUUID().toString())));
    Mockito.when(subscriptionsService.dataChanged(any(), anyBoolean()))
      .thenReturn(Future.succeededFuture());

    organisationsService.updateOrganisations(List.of(createValidOrganisation(), createValidOrganisation()))
      .onComplete(testContext.succeeding(strings -> testContext.verify(() -> {
        Mockito.verify(mockOrganisationsDatabaseService, Mockito.times(1)).saveOrganisations(anyList());
        Mockito.verify(subscriptionsService, Mockito.times(1)).dataChanged(any(), eq(false));
        testContext.completeNow();
      })));
  }

  @Test
  void deleteOrganisations(VertxTestContext testContext) {
    Mockito.when(mockOrganisationsDatabaseService.deleteOrganisations(any()))
      .thenReturn(Future.succeededFuture(List.of(UUID.randomUUID().toString())));
    Mockito.when(deletedEntitiesService.entitiesDeleted(anyList(), any()))
      .thenReturn(Future.succeededFuture());

    organisationsService.deleteOrganisations(List.of(createValidOrganisation(), createValidOrganisation()))
      .onComplete(testContext.succeeding(strings -> testContext.verify(() -> {
        Mockito.verify(mockOrganisationsDatabaseService, Mockito.times(1)).deleteOrganisations(anyList());

        Mockito.verify(deletedEntitiesService, Mockito.times(1)).entitiesDeleted(anyList(), any());
        testContext.completeNow();
      })));
  }

  @Test
  void getOrganisationsSuccess(VertxTestContext testContext) {
    Mockito.when(mockOrganisationsDatabaseService.findOrganisations(any()))
      .thenReturn(Future.succeededFuture(new LinkedList<>()));

    organisationsService.getOrganisations(new JsonObject())
      .onComplete(testContext.succeeding(strings -> testContext.verify(() -> {
        Mockito.verify(mockOrganisationsDatabaseService, Mockito.times(1)).findOrganisations(any());
        testContext.completeNow();
      })));
  }
}

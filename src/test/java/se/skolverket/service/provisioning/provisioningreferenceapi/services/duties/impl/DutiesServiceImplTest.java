package se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.impl;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.serviceproxy.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.PersonReference;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.DeletedEntitiesService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.database.DutiesDatabaseService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.model.Duty;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.PersonsService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.SubscriptionsService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.testdata.DutiesTestData.createValidDuty;
import static se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.helper.PersonHelper.validPerson;

@ExtendWith(VertxExtension.class)
class DutiesServiceImplTest {

  private DutiesDatabaseService mockDutiesDatabaseService;
  private DeletedEntitiesService deletedEntitiesService;
  private PersonsService personsService;
  private DutiesServiceImpl dutiesService;
  private SubscriptionsService subscriptionsService;

  @BeforeEach
  @DisplayName("DutiesServiceImplTest setup")
  void name(VertxTestContext testContext) {
    mockDutiesDatabaseService = Mockito.mock(DutiesDatabaseService.class);
    deletedEntitiesService = Mockito.mock(DeletedEntitiesService.class);
    personsService = Mockito.mock(PersonsService.class);
    subscriptionsService = Mockito.mock(SubscriptionsService.class);
    dutiesService = new DutiesServiceImpl(mockDutiesDatabaseService, deletedEntitiesService, personsService,
      subscriptionsService);
    testContext.completeNow();
  }

  @Test
  void getDutiesSuccess(VertxTestContext testContext) {
    Mockito.when(mockDutiesDatabaseService.findDuties(any()))
      .thenReturn(Future.succeededFuture());

    dutiesService.getDuties(new JsonObject())
      .onComplete(testContext.succeeding(strings -> testContext.verify(() -> {
        Mockito.verify(mockDutiesDatabaseService, Mockito.times(1)).findDuties(any());
        testContext.completeNow();
      })));
  }

  @Test
  void getDutiesByDutyIds(VertxTestContext testContext) {
    Mockito.when(mockDutiesDatabaseService.findDutiesByDutyIds(any()))
      .thenReturn(Future.succeededFuture());

    dutiesService.getDutiesByDutyIds(List.of("1"))
      .onComplete(testContext.succeeding(strings -> testContext.verify(() -> {
        Mockito.verify(mockDutiesDatabaseService, Mockito.times(1)).findDutiesByDutyIds(any());
        testContext.completeNow();
      })));
  }

  @Test
  void createDutiesSuccess(VertxTestContext testContext) {
    Mockito.when(mockDutiesDatabaseService.insertDuties(anyList())).thenReturn(Future.succeededFuture());
    Mockito.when(personsService.getPersonsByPersonIds(anyList()))
      .thenReturn(Future.succeededFuture(List.of(validPerson(), validPerson())));
    Mockito.when(subscriptionsService.dataChanged(any(), anyBoolean())).thenReturn(Future.succeededFuture());

    Duty duty = createValidDuty();
    duty.setPerson(new PersonReference("1", "name-test-1"));
    dutiesService.createDuties(List.of(duty, createValidDuty()))
      .onComplete(testContext.succeeding(strings -> testContext.verify(() -> {
        Mockito.verify(mockDutiesDatabaseService, Mockito.times(1)).insertDuties(anyList());
        Mockito.verify(personsService, Mockito.times(1)).getPersonsByPersonIds(anyList());
        Mockito.verify(subscriptionsService, Mockito.times(1)).dataChanged(any(), anyBoolean());
        testContext.completeNow();
      })));
  }

  @Test
  @DisplayName("Create duties resulting in Bad Request - Person Reference not found.")
  void createDutiesPersonRefNotFoundErrorTest(VertxTestContext testContext) {
    Mockito.when(mockDutiesDatabaseService.insertDuties(anyList())).thenReturn(Future.succeededFuture());
    Mockito.when(personsService.getPersonsByPersonIds(anyList()))
      .thenReturn(Future.succeededFuture(List.of(validPerson(), validPerson())));
    Mockito.when(subscriptionsService.dataChanged(any(), anyBoolean())).thenReturn(Future.succeededFuture());

    dutiesService.createDuties(List.of(createValidDuty(), createValidDuty()))
      .onComplete(testContext.failing(error -> testContext.verify(() -> {
        Mockito.verify(personsService, Mockito.times(1)).getPersonsByPersonIds(anyList());
        Mockito.verify(mockDutiesDatabaseService, Mockito.times(0)).insertDuties(anyList());
        Mockito.verify(subscriptionsService, Mockito.times(0)).dataChanged(any(), anyBoolean());
        assertEquals(ServiceException.class.getName(), error.getClass().getName());
        assertEquals(400, ((ServiceException) error).failureCode());
        assertEquals("Person reference does not exist.", error.getMessage());
        testContext.completeNow();
      })));
  }

  @Test
  @DisplayName("Update duties resulting in Bad Request - Person Reference not found.")
  void updateDutiesPersonRefNotFoundErrorTest(VertxTestContext testContext) {
    Mockito.when(mockDutiesDatabaseService.saveDuties(anyList())).thenReturn(Future.succeededFuture());
    Mockito.when(personsService.getPersonsByPersonIds(anyList()))
      .thenReturn(Future.succeededFuture(List.of(validPerson(), validPerson())));
    Mockito.when(subscriptionsService.dataChanged(any(), anyBoolean())).thenReturn(Future.succeededFuture());

    dutiesService.updateDuties(List.of(createValidDuty(), createValidDuty()))
      .onComplete(testContext.failing(error -> testContext.verify(() -> {
        Mockito.verify(personsService, Mockito.times(1)).getPersonsByPersonIds(anyList());
        Mockito.verify(mockDutiesDatabaseService, Mockito.times(0)).insertDuties(anyList());
        Mockito.verify(subscriptionsService, Mockito.times(0)).dataChanged(any(), anyBoolean());
        assertEquals(ServiceException.class.getName(), error.getClass().getName());
        assertEquals(400, ((ServiceException) error).failureCode());
        assertEquals("Person reference does not exist.", error.getMessage());
        testContext.completeNow();
      })));
  }

  @Test
  void updateDutiesSuccess(VertxTestContext testContext) {
    Mockito.when(mockDutiesDatabaseService.saveDuties(anyList())).thenReturn(Future.succeededFuture());
    Mockito.when(personsService.getPersonsByPersonIds(anyList()))
      .thenReturn(Future.succeededFuture(List.of(validPerson(), validPerson())));
    Mockito.when(subscriptionsService.dataChanged(any(), anyBoolean())).thenReturn(Future.succeededFuture());

    Duty duty = createValidDuty();
    duty.setPerson(new PersonReference("2", "name-test-2"));
    dutiesService.updateDuties(List.of(duty, createValidDuty()))
      .onComplete(testContext.succeeding(strings -> testContext.verify(() -> {
        Mockito.verify(personsService, Mockito.times(1)).getPersonsByPersonIds(anyList());
        Mockito.verify(mockDutiesDatabaseService, Mockito.times(1)).saveDuties(anyList());
        Mockito.verify(subscriptionsService, Mockito.times(1)).dataChanged(any(), anyBoolean());
        testContext.completeNow();
      })));
  }

  @Test
  void deleteDutiesSuccess(VertxTestContext testContext) {
    Mockito.when(mockDutiesDatabaseService.deleteDuties(anyList()))
      .thenReturn(Future.succeededFuture(List.of("id1", "id2")));
    Mockito.when(deletedEntitiesService.entitiesDeleted(any(), any()))
      .thenReturn(Future.succeededFuture());

    dutiesService.deleteDuties(List.of(createValidDuty(), createValidDuty()))
      .onComplete(testContext.succeeding(strings -> testContext.verify(() -> {
        Mockito.verify(mockDutiesDatabaseService, Mockito.times(1)).deleteDuties(anyList());
        testContext.completeNow();
      })));
  }
}

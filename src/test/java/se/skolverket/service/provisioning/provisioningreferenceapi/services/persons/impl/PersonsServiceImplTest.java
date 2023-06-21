package se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.impl;

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
import se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.database.PersonsDatabaseService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.SubscriptionsService;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.helper.PersonHelper.*;

@ExtendWith(VertxExtension.class)
class PersonsServiceImplTest {

  private PersonsDatabaseService mockPersonsDatabaseService;
  private DeletedEntitiesService deletedEntitiesService;
  private SubscriptionsService subscriptionsService;
  private PersonsServiceImpl personsService;


  @BeforeEach
  @DisplayName("PersonsServiceImplTest setup.")
  void setup(VertxTestContext testContext) {
    mockPersonsDatabaseService = Mockito.mock(PersonsDatabaseService.class);
    deletedEntitiesService = Mockito.mock(DeletedEntitiesService.class);
    subscriptionsService = Mockito.mock(SubscriptionsService.class);
    personsService = new PersonsServiceImpl(mockPersonsDatabaseService, deletedEntitiesService, subscriptionsService);
    testContext.completeNow();
  }

  @Test
  @DisplayName("getPersons")
  void getPersons(VertxTestContext testContext) {
    Mockito.when(mockPersonsDatabaseService.findPersons(any())).thenReturn(Future.succeededFuture(List.of(validPerson())));

    personsService.getPersons(new JsonObject())
      .onComplete(testContext.succeeding(strings -> testContext.verify(() -> {
        Mockito.verify(mockPersonsDatabaseService, Mockito.times(1)).findPersons(any());
        testContext.completeNow();
      })));
  }

  @Test
  @DisplayName("findPersonsByPersonIds")
  void findPersonsByPersonIdsTest(VertxTestContext testContext) {
    Mockito.when(mockPersonsDatabaseService.findPersonsByPersonIds(any())).thenReturn(Future.succeededFuture(List.of(validPerson())));

    personsService.getPersonsByPersonIds(List.of("1", "2", "3"))
      .onComplete(testContext.succeeding(strings -> testContext.verify(() -> {
        Mockito.verify(mockPersonsDatabaseService, Mockito.times(1)).findPersonsByPersonIds(any());
        testContext.completeNow();
      })));
  }

  @Test
  @DisplayName("insertPersons")
  void insertPersonsSuccess(VertxTestContext testContext) {
    Mockito.when(mockPersonsDatabaseService.getPersonsByEPPN(anyList())).thenReturn(Future.succeededFuture(List.of()));
    Mockito.when(mockPersonsDatabaseService.insertPersons(anyList())).thenReturn(Future.succeededFuture(List.of(UUID.randomUUID().toString())));
    Mockito.when(subscriptionsService.dataChanged(any(), anyBoolean())).thenReturn(Future.succeededFuture());

    personsService.createPersons(List.of(validPerson()))
      .onComplete(testContext.succeeding(strings -> testContext.verify(() -> {
        Mockito.verify(mockPersonsDatabaseService, Mockito.times(1)).getPersonsByEPPN(anyList());
        Mockito.verify(mockPersonsDatabaseService, Mockito.times(1)).insertPersons(anyList());
        Mockito.verify(subscriptionsService, Mockito.times(1)).dataChanged(any(), anyBoolean());
        testContext.completeNow();
      })));
  }

  @Test
  @DisplayName("insertPersons input people duplicate EPPN")
  void insertPersonsDuplicateEppn(VertxTestContext testContext) {
    personsService.createPersons(List.of(validPersonWithEppn("1"), validPersonWithEppn("1")))
      .onFailure(e -> testContext.completeNow())
      .onSuccess(s -> testContext.failNow("Create persons should not have succeeded."));
  }

  @Test
  @DisplayName("insertPersons duplicate EPPN in DB")
  void insertPersonsDuplicateEppnInDB(VertxTestContext testContext) {
    Mockito.when(mockPersonsDatabaseService.getPersonsByEPPN(anyList())).thenReturn(Future.succeededFuture(List.of(validPerson())));
    personsService.createPersons(List.of(validPerson()))
      .onFailure(e -> testContext.completeNow())
      .onSuccess(s -> testContext.failNow("Create persons should not have succeeded."));
  }

  @Test
  @DisplayName("updatePersons success")
  void updatePersonsSuccess(VertxTestContext testContext) {
    Mockito.when(mockPersonsDatabaseService.getPersonsByEPPN(anyList()))
      .thenReturn(Future.succeededFuture(List.of(
        validPersonWithEppn("2")
      )));
    Mockito.when(mockPersonsDatabaseService.savePersons(anyList())).thenReturn(Future.succeededFuture(List.of(UUID.randomUUID().toString())));
    Mockito.when(subscriptionsService.dataChanged(any(), anyBoolean())).thenReturn(Future.succeededFuture());

    personsService.updatePersons(List.of(validPersonWithEppn("1")))
      .onComplete(testContext.succeeding(strings -> testContext.verify(() -> {
        Mockito.verify(mockPersonsDatabaseService, Mockito.times(1)).savePersons(anyList());
        Mockito.verify(subscriptionsService, Mockito.times(1)).dataChanged(any(), anyBoolean());
        testContext.completeNow();
      })));
  }

  @Test
  @DisplayName("updatePersons success update EPPN")
  void updatePersonsSuccessUpdateEppn(VertxTestContext testContext) {
    Mockito.when(mockPersonsDatabaseService.getPersonsByEPPN(anyList()))
      .thenReturn(Future.succeededFuture(List.of(
        validPersonWithIdAndEppn("Dave", "1")
      )));
    Mockito.when(mockPersonsDatabaseService.savePersons(anyList())).thenReturn(Future.succeededFuture(List.of()));

    personsService.updatePersons(List.of(
      validPersonWithIdAndEppn("Dave", "2"),
      validPersonWithEppn("1"), // Taken EPPN, but update would ensure no duplicates.
      validPersonWithEppn("3")
    )).onFailure(e -> testContext.failNow("Update person should have succeeded since the resulting EPPN distribution yields no duplicates"))
      .onSuccess(s -> testContext.completeNow());
  }

  @Test
  @DisplayName("updatePersons failed duplicate input EPPN")
  void updatePersonsInputDuplicateEppn(VertxTestContext testContext) {
    personsService.updatePersons(List.of(
      validPersonWithEppn("1"),
      validPersonWithEppn("1"),
      validPersonWithEppn("3")
    )).onFailure(e -> testContext.completeNow())
      .onSuccess(s -> testContext.failNow("Update persons should have thrown an error"));
  }

  @Test
  @DisplayName("updatePersons failed DB duplicate EPPN")
  void updatePersonsDbDuplicateEppn(VertxTestContext testContext) {
    Mockito.when(mockPersonsDatabaseService.getPersonsByEPPN(anyList()))
      .thenReturn(Future.succeededFuture(List.of(
        validPersonWithEppn("1")
      )));

    personsService.updatePersons(List.of(
      validPersonWithEppn("1"), // Same EPPN, different ID -> would result in duplicate
      validPersonWithEppn("2"),
      validPersonWithEppn("3")
    )).onFailure(e -> testContext.completeNow())
      .onSuccess(s -> testContext.failNow("Update person should have failed due to conflicting EPPN"));
  }

  @Test
  @DisplayName("deletePersons")
  void deletePersons(VertxTestContext testContext) {
    Mockito.when(mockPersonsDatabaseService.deletePersons(anyList()))
      .thenReturn(Future.succeededFuture(List.of("id1", "id2")));
    Mockito.when(deletedEntitiesService.entitiesDeleted(any(), any()))
      .thenReturn(Future.succeededFuture());

    personsService.deletePersons(List.of(validPerson(), validPerson()))
      .onComplete(testContext.succeeding(strings -> testContext.verify(() -> {
        Mockito.verify(mockPersonsDatabaseService, Mockito.times(1)).deletePersons(anyList());
        testContext.completeNow();
      })));
  }
}

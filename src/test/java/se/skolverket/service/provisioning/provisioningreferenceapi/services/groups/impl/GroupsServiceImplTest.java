package se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.impl;

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
import se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.database.GroupsDatabaseService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.PersonsService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.helper.PersonHelper;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.SubscriptionsService;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.testdata.GroupTestData.createValidGroup;

@ExtendWith(VertxExtension.class)
class GroupsServiceImplTest {

  private GroupsDatabaseService mockGroupsDatabaseService;

  private GroupsServiceImpl groupsService;
  private DeletedEntitiesService deletedEntitiesService;
  private PersonsService personsService;
  private SubscriptionsService subscriptionsService;

  @BeforeEach
  @DisplayName("GroupsServiceImplTest setup")
  void setup(VertxTestContext testContext) {
    mockGroupsDatabaseService = Mockito.mock(GroupsDatabaseService.class);
    deletedEntitiesService = Mockito.mock(DeletedEntitiesService.class);
    personsService = Mockito.mock(PersonsService.class);
    subscriptionsService = Mockito.mock(SubscriptionsService.class);
    groupsService = new GroupsServiceImpl(mockGroupsDatabaseService, deletedEntitiesService,
      personsService, subscriptionsService);
    testContext.completeNow();
  }

  @Test
  @DisplayName("getGroups")
  void getGroups(VertxTestContext testContext) {
    Mockito.when(mockGroupsDatabaseService.findGroups(any())).thenReturn(Future.succeededFuture(List.of(createValidGroup())));

    groupsService.getGroups(new JsonObject())
      .onComplete(testContext.succeeding(strings -> testContext.verify(() -> {
        Mockito.verify(mockGroupsDatabaseService, Mockito.times(1)).findGroups(any());
        testContext.completeNow();
      })));
  }

  @Test
  @DisplayName("getGroupsByGroupIds")
  void getGroupsByGroupIds(VertxTestContext testContext) {
    Mockito.when(mockGroupsDatabaseService.findGroupsByGroupId(any())).thenReturn(Future.succeededFuture(List.of(createValidGroup())));

    groupsService.getGroupsByGroupId(List.of("1"))
      .onComplete(testContext.succeeding(strings -> testContext.verify(() -> {
        Mockito.verify(mockGroupsDatabaseService, Mockito.times(1)).findGroupsByGroupId(any());
        testContext.completeNow();
      })));
  }

  @Test
  void createGroupsSuccess(VertxTestContext testContext) {
    Mockito.when(mockGroupsDatabaseService.insertGroups(anyList())).thenReturn(Future.succeededFuture());
    Mockito.when(subscriptionsService.dataChanged(any(), anyBoolean())).thenReturn(Future.succeededFuture());
    Mockito.when(personsService.getPersonsByPersonIds(anyList()))
      .thenReturn(Future.succeededFuture(List.of(PersonHelper.validPerson(), PersonHelper.validPerson())));

    groupsService.createGroups(List.of(createValidGroup(), createValidGroup()))
      .onComplete(testContext.succeeding(strings -> testContext.verify(() -> {
        Mockito.verify(mockGroupsDatabaseService, Mockito.times(1)).insertGroups(anyList());
        Mockito.verify(personsService, Mockito.times(1)).getPersonsByPersonIds(anyList());
        Mockito.verify(subscriptionsService, Mockito.times(1)).dataChanged(any(), anyBoolean());
        testContext.completeNow();
      })));
  }

  @Test
  void createGroupsFailMissingPerson(VertxTestContext testContext) {
    Mockito.when(mockGroupsDatabaseService.insertGroups(anyList())).thenReturn(Future.succeededFuture());

    Mockito.when(personsService.getPersonsByPersonIds(anyList()))
      .thenReturn(Future.succeededFuture(List.of()));
    Mockito.when(subscriptionsService.dataChanged(any(), anyBoolean())).thenReturn(Future.succeededFuture());

    groupsService.createGroups(List.of(createValidGroup(), createValidGroup()))
      .onComplete(testContext.failing(t -> testContext.verify(() -> {
        Mockito.verify(mockGroupsDatabaseService, Mockito.times(0)).insertGroups(anyList());
        Mockito.verify(personsService, Mockito.times(1)).getPersonsByPersonIds(anyList());
        Mockito.verify(subscriptionsService, Mockito.times(0)).dataChanged(any(), anyBoolean());
        testContext.completeNow();
      })));
  }

  @Test
  void updateGroupsSuccess(VertxTestContext testContext) {
    Mockito.when(mockGroupsDatabaseService.saveGroups(anyList()))
      .thenReturn(Future.succeededFuture(List.of(UUID.randomUUID().toString(), UUID.randomUUID().toString())));
    Mockito.when(personsService.getPersonsByPersonIds(anyList()))
      .thenReturn(Future.succeededFuture(List.of(PersonHelper.validPerson(), PersonHelper.validPerson())));
    Mockito.when(subscriptionsService.dataChanged(any(), anyBoolean())).thenReturn(Future.succeededFuture());

    groupsService.updateGroups(List.of(createValidGroup(), createValidGroup()))
      .onComplete(testContext.succeeding(strings -> testContext.verify(() -> {
        Mockito.verify(mockGroupsDatabaseService, Mockito.times(1)).saveGroups(anyList());
        Mockito.verify(personsService, Mockito.times(1)).getPersonsByPersonIds(anyList());
        Mockito.verify(subscriptionsService, Mockito.times(1)).dataChanged(any(), anyBoolean());
        testContext.completeNow();
      })));
  }

  @Test
  void updateGroupsFailMissingPerson(VertxTestContext testContext) {
    Mockito.when(mockGroupsDatabaseService.saveGroups(anyList()))
      .thenReturn(Future.succeededFuture(List.of(UUID.randomUUID().toString(), UUID.randomUUID().toString())));

    Mockito.when(personsService.getPersonsByPersonIds(anyList()))
      .thenReturn(Future.succeededFuture(List.of(PersonHelper.validPerson())));
    Mockito.when(subscriptionsService.dataChanged(any(), anyBoolean())).thenReturn(Future.succeededFuture());

    groupsService.updateGroups(List.of(createValidGroup(), createValidGroup()))
      .onComplete(testContext.failing(t -> testContext.verify(() -> {
        Mockito.verify(personsService, Mockito.times(1)).getPersonsByPersonIds(anyList());
        Mockito.verify(mockGroupsDatabaseService, Mockito.times(0)).saveGroups(anyList());
        Mockito.verify(subscriptionsService, Mockito.times(0)).dataChanged(any(), anyBoolean());
        Mockito.verify(subscriptionsService, Mockito.times(0)).dataChanged(any(), anyBoolean());
        testContext.completeNow();
      })));
  }

  @Test
  void deleteGroups(VertxTestContext testContext) {
    Mockito.when(mockGroupsDatabaseService.deleteGroups(anyList()))
      .thenReturn(Future.succeededFuture(List.of("id1", "id2")));
    Mockito.when(deletedEntitiesService.entitiesDeleted(any(), any()))
      .thenReturn(Future.succeededFuture());

    groupsService.deleteGroups(List.of(createValidGroup(), createValidGroup()))
      .onComplete(testContext.succeeding(strings -> testContext.verify(() -> {
        Mockito.verify(mockGroupsDatabaseService, Mockito.times(1)).deleteGroups(anyList());
        testContext.completeNow();
      })));
  }
}

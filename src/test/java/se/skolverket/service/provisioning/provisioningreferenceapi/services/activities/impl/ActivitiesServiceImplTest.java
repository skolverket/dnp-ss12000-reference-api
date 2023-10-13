package se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.impl;


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
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.DutyAssignment;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.ObjectReference;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.database.ActivitiesDatabaseService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.model.Activity;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.DeletedEntitiesService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.DutiesService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.GroupsService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.SubscriptionsService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.helper.ActivitiesHelper.validActivity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.testdata.DutiesTestData.createValidDuty;
import static se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.testdata.GroupTestData.createValidGroup;

@ExtendWith(VertxExtension.class)
public class ActivitiesServiceImplTest {
  private ActivitiesDatabaseService mockActivitiesDatabaseService;

  private ActivitiesServiceImpl activitiesService;
  private DeletedEntitiesService deletedEntitiesService;

  private GroupsService mockGroupsService;
  private DutiesService mockDutiesService;

  private SubscriptionsService mockSubscriptionService;

  @BeforeEach
  @DisplayName("ActivitiesServiceImplTest setup")
  void setup(VertxTestContext testContext) {
    mockActivitiesDatabaseService = Mockito.mock(ActivitiesDatabaseService.class);
    deletedEntitiesService = Mockito.mock(DeletedEntitiesService.class);
    mockGroupsService = Mockito.mock(GroupsService.class);
    mockDutiesService = Mockito.mock(DutiesService.class);
    mockSubscriptionService = Mockito.mock(SubscriptionsService.class);
    activitiesService = new ActivitiesServiceImpl(mockActivitiesDatabaseService, deletedEntitiesService,
      mockGroupsService, mockDutiesService, mockSubscriptionService);
    testContext.completeNow();
  }

  @Test
  @DisplayName("Get activities success.")
  void getActivitiesSuccess(VertxTestContext testContext) {
    Mockito.when(mockActivitiesDatabaseService.findActivities(any()))
      .thenReturn(Future.succeededFuture());

    activitiesService.findActivities(new JsonObject())
      .onComplete(testContext.succeeding(strings -> testContext.verify(() -> {
        Mockito.verify(mockActivitiesDatabaseService, Mockito.times(1)).findActivities(any());
        testContext.completeNow();
      })));
  }

  @Test
  @DisplayName("Create activities success.")
  void createActivitiesSuccess(VertxTestContext testContext) {
    Mockito.when(mockGroupsService.getGroupsByGroupId(anyList())).thenReturn(Future.succeededFuture(List.of(createValidGroup())));
    Mockito.when(mockDutiesService.getDutiesByDutyIds(anyList())).thenReturn(Future.succeededFuture(List.of(createValidDuty())));
    Mockito.when(mockActivitiesDatabaseService.insertActivities(anyList())).thenReturn(Future.succeededFuture());
    Mockito.when(mockSubscriptionService.dataChanged(any(), anyBoolean())).thenReturn(Future.succeededFuture());

    activitiesService.createActivities(List.of(validActivity()))
      .onComplete(testContext.succeeding(strings -> testContext.verify(() -> {
        Mockito.verify(mockGroupsService, Mockito.times(1)).getGroupsByGroupId(anyList());
        Mockito.verify(mockDutiesService, Mockito.times(1)).getDutiesByDutyIds(anyList());
        Mockito.verify(mockActivitiesDatabaseService, Mockito.times(1)).insertActivities(anyList());
        Mockito.verify(mockSubscriptionService, Mockito.times(1)).dataChanged(any(), anyBoolean());
        testContext.completeNow();
      })));
  }

  @Test
  @DisplayName("Create activities resulting in Bad Request - Group reference not found.")
  void createActivitiesGroupRefNotFoundErrorTest(VertxTestContext testContext) {
    Mockito.when(mockGroupsService.getGroupsByGroupId(anyList())).thenReturn(Future.succeededFuture(List.of(createValidGroup())));
    Mockito.when(mockDutiesService.getDutiesByDutyIds(anyList())).thenReturn(Future.succeededFuture(List.of(createValidDuty(), createValidDuty())));
    Mockito.when(mockActivitiesDatabaseService.insertActivities(anyList())).thenReturn(Future.succeededFuture());

    //Group validation should throw exception since mockGroupService only returns 1 Group.
    activitiesService.createActivities(List.of(validActivity(), validActivity()))
      .onComplete(testContext.failing(error -> testContext.verify(() -> {
        assertEquals(ServiceException.class.getName(), error.getClass().getName());
        assertEquals(400, ((ServiceException) error).failureCode());
        assertEquals("Group reference does not exist.", error.getMessage());
        Mockito.verify(mockGroupsService, Mockito.times(1)).getGroupsByGroupId(anyList());
        Mockito.verify(mockDutiesService, Mockito.times(0)).getDutiesByDutyIds(anyList());
        Mockito.verify(mockSubscriptionService, Mockito.times(0)).dataChanged(any(), anyBoolean());
        testContext.completeNow();
      })));
  }

  @Test
  @DisplayName("Create activities resulting in Bad Request - Teacher reference not found.")
  void createActivitiesTeacherRefNotFoundErrorTest(VertxTestContext testContext) {
    Mockito.when(mockGroupsService.getGroupsByGroupId(anyList())).thenReturn(Future.succeededFuture(List.of(createValidGroup(), createValidGroup())));
    Mockito.when(mockDutiesService.getDutiesByDutyIds(anyList())).thenReturn(Future.succeededFuture(List.of(createValidDuty())));
    Mockito.when(mockActivitiesDatabaseService.insertActivities(anyList())).thenReturn(Future.succeededFuture());
    Mockito.when(mockSubscriptionService.dataChanged(any(), anyBoolean())).thenReturn(Future.succeededFuture());

    //Duty/Teacher validation should throw exception since mockDutiesService only returns 1 Duty.
    activitiesService.createActivities(List.of(validActivity(), validActivity()))
      .onComplete(testContext.failing(error -> testContext.verify(() -> {
        assertEquals(ServiceException.class.getName(), error.getClass().getName());
        assertEquals(400, ((ServiceException) error).failureCode());
        assertEquals("Teacher reference does not exist.", error.getMessage());
        Mockito.verify(mockGroupsService, Mockito.times(1)).getGroupsByGroupId(anyList());
        Mockito.verify(mockDutiesService, Mockito.times(1)).getDutiesByDutyIds(anyList());
        Mockito.verify(mockSubscriptionService, Mockito.times(0)).dataChanged(any(), anyBoolean());
        testContext.completeNow();
      })));
  }

  @Test
  void updateActivitiesSuccess(VertxTestContext testContext) {
    Mockito.when(mockGroupsService.getGroupsByGroupId(anyList())).thenReturn(Future.succeededFuture(List.of(createValidGroup())));
    Mockito.when(mockDutiesService.getDutiesByDutyIds(anyList())).thenReturn(Future.succeededFuture(List.of(createValidDuty())));
    Mockito.when(mockActivitiesDatabaseService.saveActivities(anyList()))
      .thenReturn(Future.succeededFuture(List.of(UUID.randomUUID().toString(), UUID.randomUUID().toString())));
    Mockito.when(mockSubscriptionService.dataChanged(any(), anyBoolean())).thenReturn(Future.succeededFuture());

    activitiesService.updateActivities(List.of(validActivity()))
      .onComplete(testContext.succeeding(strings -> testContext.verify(() -> {
        Mockito.verify(mockGroupsService, Mockito.times(1)).getGroupsByGroupId(anyList());
        Mockito.verify(mockDutiesService, Mockito.times(1)).getDutiesByDutyIds(anyList());
        Mockito.verify(mockActivitiesDatabaseService, Mockito.times(1)).saveActivities(anyList());
        Mockito.verify(mockSubscriptionService, Mockito.times(1)).dataChanged(any(), anyBoolean());
        testContext.completeNow();
      })));
  }

  @Test
  @DisplayName("Update activities resulting in Bad Request - Group reference not found.")
  void updateActivitiesGroupRefNotFoundErrorTest(VertxTestContext testContext) {
    Mockito.when(mockGroupsService.getGroupsByGroupId(anyList())).thenReturn(Future.succeededFuture(List.of(createValidGroup())));
    Mockito.when(mockDutiesService.getDutiesByDutyIds(anyList())).thenReturn(Future.succeededFuture(List.of(createValidDuty(), createValidDuty())));
    Mockito.when(mockActivitiesDatabaseService.insertActivities(anyList())).thenReturn(Future.succeededFuture());
    Mockito.when(mockSubscriptionService.dataChanged(any(), anyBoolean())).thenReturn(Future.succeededFuture());

    //Group validation should throw exception since mockGroupService only returns 1 Group.
    activitiesService.createActivities(List.of(validActivity(), validActivity()))
      .onComplete(testContext.failing(error -> testContext.verify(() -> {
        assertEquals(ServiceException.class.getName(), error.getClass().getName());
        assertEquals(400, ((ServiceException) error).failureCode());
        assertEquals("Group reference does not exist.", error.getMessage());
        Mockito.verify(mockGroupsService, Mockito.times(1)).getGroupsByGroupId(anyList());
        Mockito.verify(mockDutiesService, Mockito.times(0)).getDutiesByDutyIds(anyList());
        Mockito.verify(mockSubscriptionService, Mockito.times(0)).dataChanged(any(), anyBoolean());
        testContext.completeNow();
      })));
  }

  @Test
  @DisplayName("Update activities resulting in Bad Request - Teacher reference not found.")
  void updateActivitiesTeacherRefNotFoundErrorTest(VertxTestContext testContext) {
    Mockito.when(mockGroupsService.getGroupsByGroupId(anyList())).thenReturn(Future.succeededFuture(List.of(createValidGroup(), createValidGroup())));
    Mockito.when(mockDutiesService.getDutiesByDutyIds(anyList())).thenReturn(Future.succeededFuture(List.of(createValidDuty())));
    Mockito.when(mockActivitiesDatabaseService.insertActivities(anyList())).thenReturn(Future.succeededFuture());
    Mockito.when(mockSubscriptionService.dataChanged(any(), anyBoolean())).thenReturn(Future.succeededFuture());

    //Duty/Teacher validation should throw exception since mockDutiesService only returns 1 Duty.
    activitiesService.updateActivities(List.of(validActivity(), validActivity()))
      .onComplete(testContext.failing(error -> testContext.verify(() -> {
        assertEquals(ServiceException.class.getName(), error.getClass().getName());
        assertEquals(400, ((ServiceException) error).failureCode());
        assertEquals("Teacher reference does not exist.", error.getMessage());
        testContext.completeNow();
      })));
  }

  @Test
  @DisplayName("Delete activities")
  void deleteActivitiesSuccess(VertxTestContext testContext) {
    Mockito.when(mockActivitiesDatabaseService.deleteActivities(anyList())).thenReturn(Future.succeededFuture(List.of("id1", "id2")));
    Mockito.when(deletedEntitiesService.entitiesDeleted(any(), any())).thenReturn(Future.succeededFuture());

    activitiesService.deleteActivities(List.of(validActivity(), validActivity()))
      .onComplete(testContext.succeeding(strings -> testContext.verify(() -> {
        Mockito.verify(mockActivitiesDatabaseService, Mockito.times(1)).deleteActivities(anyList());
        testContext.completeNow();
      })));
  }

  @Test
  void collectGroupIdsTest() {
    Activity activity = new Activity();
    String groupId = UUID.randomUUID().toString();
    activity.setGroups(List.of(new ObjectReference(groupId, "Test group")));

    List<String> groupIds = new ArrayList<>(activitiesService.collectGroupIds(List.of(activity, activity, activity)));
    assertEquals(1, groupIds.size());
    assertEquals(groupId, groupIds.get(0));
  }

  @Test
  void collectDutyAssignmentIdsTest() {
    Activity activity = new Activity();
    String dutyId = UUID.randomUUID().toString();
    activity.setTeachers(List.of(DutyAssignment.builder()
      .duty(new ObjectReference(dutyId, "Test duty"))
      .build())
    );
    List<String> dutyAssignmentIds = new ArrayList<>(activitiesService.collectDutyAssignmentIds(List.of(activity, activity, activity)));
    assertEquals(1, dutyAssignmentIds.size());
    assertEquals(dutyId, dutyAssignmentIds.get(0));
  }
}

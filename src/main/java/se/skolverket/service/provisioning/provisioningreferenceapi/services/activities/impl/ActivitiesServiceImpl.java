package se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.impl;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.WriteStream;
import io.vertx.serviceproxy.ServiceException;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.StreamingService;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.ResourceType;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.ActivitiesService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.database.ActivitiesDatabaseService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.model.Activity;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.DeletedEntitiesService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.DutiesService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.GroupsService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.SubscriptionsService;

import java.util.ArrayList;
import java.util.List;

public class ActivitiesServiceImpl extends StreamingService implements ActivitiesService {

  private final ActivitiesDatabaseService activitiesDatabaseService;
  private final DeletedEntitiesService deletedEntitiesService;
  private final GroupsService groupsService;
  private final DutiesService dutiesService;
  private final SubscriptionsService subscriptionsService;

  public ActivitiesServiceImpl(ActivitiesDatabaseService activitiesDatabaseService, DeletedEntitiesService deletedEntitiesService,
                               GroupsService groupsService, DutiesService dutiesService, SubscriptionsService subscriptionsService) {
    this.activitiesDatabaseService = activitiesDatabaseService;
    this.deletedEntitiesService = deletedEntitiesService;
    this.groupsService = groupsService;
    this.dutiesService = dutiesService;
    this.subscriptionsService = subscriptionsService;
  }

  @Override
  public Future<List<String>> createActivities(List<Activity> activities) {
    return validateActivityRelationships(activities)
      .compose(Void -> activitiesDatabaseService.insertActivities(activities))
      .onSuccess(event -> subscriptionsService.dataChanged(ResourceType.ACTIVITY, false));
  }

  @Override
  public Future<List<String>> updateActivities(List<Activity> activities) {
    return validateActivityRelationships(activities)
      .compose(Void -> activitiesDatabaseService.saveActivities(activities))
      .onSuccess(event -> subscriptionsService.dataChanged(ResourceType.ACTIVITY, false));
  }

  @Override
  public Future<Void> deleteActivities(List<Activity> activities) {
    return activitiesDatabaseService.deleteActivities(activities)
      .compose(deletedIds -> {
        if (deletedIds.isEmpty()) return Future.succeededFuture();
        return deletedEntitiesService.entitiesDeleted(deletedIds, ResourceType.ACTIVITY);
      });
  }

  /**
   * Get persons with a streamed response. This is more scalable and uses less memory than getPersons for large amounts of data.
   *
   * @param bufferWriteStream A write stream that will receive the streamed data as Json Format `{ "data": [data array]}` (as buffer).
   * @param queryParams       Query parameters for the person query.
   * @return Future that is completed when the stream has ended.
   */
  @Override
  public Future<Void> getStream(WriteStream<Buffer> bufferWriteStream, JsonObject queryParams) {
    return activitiesDatabaseService.findActivitiesStream(queryParams)
      .compose(stream -> streamProcessor(stream, bufferWriteStream));
  }

  @Override
  public Future<List<Activity>> getActivities(JsonObject queryParams) {
    return activitiesDatabaseService.findActivities(queryParams);
  }

  private Future<Void> validateActivityRelationships(List<Activity> activities) {
    List<String> groupIds = new ArrayList<>(collectGroupIds(activities));
    List<String> dutyAssignmentIds = new ArrayList<>(collectDutyAssignmentIds(activities));

    return groupsService.getGroupsByGroupId(groupIds).compose(groups -> {
      if (groupIds.size() == groups.size()) {
        return dutiesService.getDutiesByDutyIds(dutyAssignmentIds).compose(duties -> {
          if (dutyAssignmentIds.size() == duties.size()) {
            return Future.succeededFuture();
          }
          return Future.failedFuture(new ServiceException(400, "Teacher reference does not exist."));
        });
      }
      return Future.failedFuture(new ServiceException(400, "Group reference does not exist."));
    });
  }
}

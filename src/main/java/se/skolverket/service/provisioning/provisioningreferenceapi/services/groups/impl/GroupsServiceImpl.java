package se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.impl;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.WriteStream;
import io.vertx.serviceproxy.ServiceException;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.StreamingService;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.ResourceType;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.DeletedEntitiesService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.GroupsService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.database.GroupsDatabaseService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.model.Group;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.PersonsService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.SubscriptionsService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class GroupsServiceImpl extends StreamingService implements GroupsService {

  private final GroupsDatabaseService databaseService;
  private final DeletedEntitiesService deletedEntitiesService;
  private final PersonsService personsService;
  private final SubscriptionsService subscriptionsService;

  public GroupsServiceImpl(GroupsDatabaseService databaseService,
                           DeletedEntitiesService deletedEntitiesService,
                           PersonsService personsService, SubscriptionsService subscriptionsService) {
    this.databaseService = databaseService;
    this.deletedEntitiesService = deletedEntitiesService;
    this.personsService = personsService;
    this.subscriptionsService = subscriptionsService;
  }

  @Override
  public Future<List<String>> createGroups(List<Group> groups) {
    List<String> personIds = new ArrayList<>(collectPersonRefIds(groups));
    return personsService.getPersonsByPersonIds(personIds).compose(persons -> {
      if (personIds.size() == persons.size()) {
        return databaseService.insertGroups(groups)
          .onSuccess(e -> subscriptionsService.dataChanged(ResourceType.GROUP, false));
      }
      return Future.failedFuture(new ServiceException(400, "Person reference does not exist."));
    });
  }

  @Override
  public Future<List<String>> updateGroups(List<Group> groups) {
    List<String> personIds = new ArrayList<>(collectPersonRefIds(groups));
    return personsService.getPersonsByPersonIds(personIds).compose(persons -> {
      if (personIds.size() == persons.size()) {
        return databaseService.saveGroups(groups)
          .onSuccess(e -> subscriptionsService.dataChanged(ResourceType.GROUP, false));
      }
      return Future.failedFuture(new ServiceException(400, "Person reference does not exist."));
    });
  }

  @Override
  public Future<Void> deleteGroups(List<Group> groups) {
    return databaseService.deleteGroups(groups)
      .compose(deletedIds -> {
        if (deletedIds.isEmpty()) return Future.succeededFuture();
        return deletedEntitiesService.entitiesDeleted(deletedIds, ResourceType.GROUP);
      });
  }

  @Override
  public Future<List<Group>> getGroups(JsonObject queryOptions) {
    return databaseService.findGroups(queryOptions);
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
    return databaseService.findGroupsStream(queryParams)
      .compose(stream -> streamProcessor(stream, bufferWriteStream, queryParams));
  }
  @Override
  public Future<List<Group>> getGroupsByGroupId(List<String> groupIds) {
    return databaseService.findGroupsByGroupId(groupIds);
  }

  private Set<String> collectPersonRefIds(List<Group> groups) {
    return groups.stream()
      .map(Group::getGroupMemberships)
      .filter(Objects::nonNull)
      .flatMap(List::stream)
      .map(groupMembership -> groupMembership.getPerson().getId())
      .collect(Collectors.toSet());
  }
}

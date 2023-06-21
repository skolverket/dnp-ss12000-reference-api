package se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.database.impl;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.DatabaseServiceHelper;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.database.GroupsDatabaseService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.model.Group;

import java.util.List;
import java.util.stream.Collectors;

import io.vertx.core.Future;

@Slf4j
public class GroupsDatabaseServiceImpl implements GroupsDatabaseService {

  private final MongoClient mongoClient;

  private static final String GROUPS_COLLECTION_NAME = "groups";

  public GroupsDatabaseServiceImpl(MongoClient mongoClient) {
    this.mongoClient = mongoClient;
  }

  @Override
  public Future<List<String>> saveGroups(List<Group> groups) {
    return DatabaseServiceHelper.saveDataTypes(mongoClient, GROUPS_COLLECTION_NAME, groups);
  }

  @Override
  public Future<List<String>> insertGroups(List<Group> groups) {
    return DatabaseServiceHelper.insertDataTypes(mongoClient, GROUPS_COLLECTION_NAME, groups);
  }

  @Override
  public Future<List<String>> deleteGroups(List<Group> groups) {
    return DatabaseServiceHelper.deleteDataTypes(mongoClient, GROUPS_COLLECTION_NAME, groups);
  }

  @Override
  public Future<List<Group>> findGroups(JsonObject queryOptions) {
    return DatabaseServiceHelper.findDataTypes(mongoClient, GROUPS_COLLECTION_NAME, queryOptions)
      .compose(jsonObjects -> Future.succeededFuture(jsonObjects.stream()
        .map(Group::fromBson).collect(Collectors.toList())));
  }

  @Override
  public Future<List<Group>> findGroupsByGroupId(List<String> groupIds) {
    return DatabaseServiceHelper.findDataTypesByIds(mongoClient, GROUPS_COLLECTION_NAME, groupIds)
      .compose(jsonObjects -> Future.succeededFuture(jsonObjects.stream()
        .map(Group::fromBson).collect(Collectors.toList())));
  }
}

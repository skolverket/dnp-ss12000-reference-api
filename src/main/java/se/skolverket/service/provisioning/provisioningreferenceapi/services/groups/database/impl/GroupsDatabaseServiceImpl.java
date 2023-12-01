package se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.database.impl;

import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
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

  private static final String COLLECTION_NAME = "groups";

  public GroupsDatabaseServiceImpl(MongoClient mongoClient) {
    this.mongoClient = mongoClient;
  }

  @Override
  public Future<List<String>> saveGroups(List<Group> groups) {
    return DatabaseServiceHelper.saveDataTypes(mongoClient, COLLECTION_NAME, groups);
  }

  @Override
  public Future<List<String>> insertGroups(List<Group> groups) {
    return DatabaseServiceHelper.insertDataTypes(mongoClient, COLLECTION_NAME, groups);
  }

  @Override
  public Future<List<String>> deleteGroups(List<Group> groups) {
    return DatabaseServiceHelper.deleteDataTypes(mongoClient, COLLECTION_NAME, groups);
  }

  @Override
  public Future<List<Group>> findGroups(JsonObject queryOptions) {
    return DatabaseServiceHelper.findDataTypes(mongoClient, COLLECTION_NAME, queryOptions)
      .compose(jsonObjects -> Future.succeededFuture(jsonObjects.stream()
        .map(Group::fromBson).collect(Collectors.toList())));
  }

  @Override
  public Future<ReadStream<JsonObject>> findGroupsStream(JsonObject queryOptions) {
    try {
      return DatabaseServiceHelper.findDataTypesStream(mongoClient, COLLECTION_NAME, queryOptions);
    } catch (Exception e) {
      log.error("Unexpected error.", e);
      return Future.failedFuture(e);
    }
  }

  @Override
  public Future<List<Group>> findGroupsByGroupId(List<String> groupIds) {
    return DatabaseServiceHelper.findDataTypesByIds(mongoClient, COLLECTION_NAME, groupIds)
      .compose(jsonObjects -> Future.succeededFuture(jsonObjects.stream()
        .map(Group::fromBson).collect(Collectors.toList())));
  }
}

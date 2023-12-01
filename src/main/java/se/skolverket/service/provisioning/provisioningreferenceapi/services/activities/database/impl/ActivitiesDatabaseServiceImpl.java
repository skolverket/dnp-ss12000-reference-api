package se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.database.impl;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.mongo.MongoClient;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.DatabaseServiceHelper;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.database.ActivitiesDatabaseService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.model.Activity;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
public class ActivitiesDatabaseServiceImpl implements ActivitiesDatabaseService {

  private final MongoClient mongoClient;

  private static final String COLLECTION_NAME = "activities";

  public ActivitiesDatabaseServiceImpl(MongoClient mongoClient) {
    this.mongoClient = mongoClient;
  }

  @Override
  public Future<List<String>> insertActivities(List<Activity> activities) {
    return DatabaseServiceHelper.insertDataTypes(mongoClient, COLLECTION_NAME, activities);
  }

  @Override
  public Future<List<String>> saveActivities(List<Activity> activities) {
    return DatabaseServiceHelper.saveDataTypes(mongoClient, COLLECTION_NAME, activities);
  }

  @Override
  public Future<List<String>> deleteActivities(List<Activity> activities) {
    return DatabaseServiceHelper.deleteDataTypes(mongoClient, COLLECTION_NAME, activities);
  }

  @Override
  public Future<List<Activity>> findActivities(JsonObject queryOptions) {
    return DatabaseServiceHelper.findDataTypes(mongoClient, COLLECTION_NAME, queryOptions)
      .compose(jsonObjects -> Future.succeededFuture(jsonObjects.stream().map(Activity::fromBson).collect(Collectors.toList())));
  }

  @Override
  public Future<ReadStream<JsonObject>> findActivitiesStream(JsonObject queryOptions) {
    try {
      return DatabaseServiceHelper.findDataTypesStream(mongoClient, COLLECTION_NAME, queryOptions);
    } catch (Exception e) {
      log.error("Unexpected error.", e);
      return Future.failedFuture(e);
    }
  }

}

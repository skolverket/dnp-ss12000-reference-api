package se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.database;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.model.Activity;

import java.util.List;

public interface ActivitiesDatabaseService {

  Future<List<String>> insertActivities(List<Activity> activities);

  Future<List<String>> saveActivities(List<Activity> activities);

  Future<List<String>> deleteActivities(List<Activity> anyList);

  Future<List<Activity>> findActivities(JsonObject queryOptions);

  Future<List<Activity>> findActivitiesByActivityIds(List<String> activityIds);

  Future<ReadStream<JsonObject>> findActivitiesStream(JsonObject queryOptions);
}

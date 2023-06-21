package se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.database;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.model.Group;

import java.util.List;

public interface GroupsDatabaseService {
  Future<List<String>> saveGroups(List<Group> groups);

  Future<List<String>> insertGroups(List<Group> groups);

  Future<List<String>> deleteGroups(List<Group> groups);

  Future<List<Group>> findGroups(JsonObject queryOptions);

  Future<List<Group>> findGroupsByGroupId(List<String> groupIds);
}

package se.skolverket.service.provisioning.provisioningreferenceapi.services.groups;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.ProxyIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.model.Group;

import java.util.List;

@ProxyGen
@VertxGen
public interface GroupsService {

  // Factory methods to create an instance and a proxy

  String ADDRESS = "groups-service";

  @ProxyIgnore
  @GenIgnore
  static GroupsService createProxy(Vertx vertx) {
    return new GroupsServiceVertxEBProxy(vertx, GroupsService.ADDRESS);
  }

  Future<List<String>> createGroups(List<Group> groups);

  Future<List<String>> updateGroups(List<Group> groups);

  Future<Void> deleteGroups(List<Group> groups);

  Future<List<Group>> getGroups(JsonObject queryOptions);

  Future<List<Group>> getGroupsByGroupId(List<String> groupIds);
}

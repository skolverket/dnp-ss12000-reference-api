package se.skolverket.service.provisioning.provisioningreferenceapi.services.activities;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.ProxyIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.ObjectReference;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.model.Activity;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ProxyGen
@VertxGen
public interface ActivitiesService {
  // Factory methods to create an instance and a proxy

  String ADDRESS = "activities-service";

  @ProxyIgnore
  @GenIgnore
  static ActivitiesService createProxy(Vertx vertx) {
    return new ActivitiesServiceVertxEBProxy(vertx, ActivitiesService.ADDRESS);
  }

  Future<List<String>> createActivities(List<Activity> activities);

  Future<List<String>> updateActivities(List<Activity> activities);

  Future<Void> deleteActivities(List<Activity> activities);

  Future<List<Activity>> getActivities(JsonObject queryParams);

  @ProxyIgnore
  @GenIgnore
  default Set<String> collectGroupIds(List<Activity> activities) {
    return activities.stream().flatMap(a -> a.getGroups().stream().map(ObjectReference::getId)).collect(Collectors.toSet());
  }

  @ProxyIgnore
  @GenIgnore
  default Set<String> collectDutyAssignmentIds(List<Activity> activities) {
    return activities.stream()
      .filter(a -> a.getTeachers() != null)
      .flatMap(a -> a.getTeachers().stream()
        .map(t -> t.getDuty().getId()))
      .collect(Collectors.toSet());
  }
}

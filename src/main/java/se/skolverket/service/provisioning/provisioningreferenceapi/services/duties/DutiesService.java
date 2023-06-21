package se.skolverket.service.provisioning.provisioningreferenceapi.services.duties;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.ProxyIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.DeletedEntitiesService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.database.DutiesDatabaseService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.impl.DutiesServiceImpl;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.model.Duty;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.PersonsService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.SubscriptionsService;

import java.util.List;

@ProxyGen
@VertxGen
public interface DutiesService {

  // Factory methods to create an instance and a proxy

  String DUTIES_ADDRESS = "duties-service";

  @ProxyIgnore
  @GenIgnore
  static DutiesService create(DutiesDatabaseService dutiesDatabaseService, DeletedEntitiesService deletedEntitiesService,
                              PersonsService personsService, SubscriptionsService subscriptionsService) {
    return new DutiesServiceImpl(dutiesDatabaseService, deletedEntitiesService, personsService, subscriptionsService);
  }

  @ProxyIgnore
  @GenIgnore
  static DutiesService createProxy(Vertx vertx) {
    return new DutiesServiceVertxEBProxy(vertx, DUTIES_ADDRESS);
  }

  Future<List<String>> createDuties(List<Duty> duties);

  Future<List<String>> updateDuties(List<Duty> duties);

  Future<Void> deleteDuties(List<Duty> duties);

  Future<List<Duty>> getDuties(JsonObject queryParams);

  Future<List<Duty>> getDutiesByDutyIds(List<String> dutyIds);

}

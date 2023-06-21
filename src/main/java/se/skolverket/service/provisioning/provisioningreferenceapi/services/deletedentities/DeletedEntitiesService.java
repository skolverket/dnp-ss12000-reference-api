package se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.ProxyIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.ResourceType;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.database.DeletedEntitiesDatabaseService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.impl.DeletedEntitiesServiceImpl;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.model.DeletedEntity;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.SubscriptionsService;

import java.util.List;

@ProxyGen
@VertxGen
public interface DeletedEntitiesService {

  // Factory methods to create an instance and a proxy

  String DELETED_ENTITIES_ADDRESS = "deletedEntities-service";

  @ProxyIgnore
  @GenIgnore
  static DeletedEntitiesService create(DeletedEntitiesDatabaseService deletedEntitiesDatabaseService,
                                       SubscriptionsService subscriptionsService) {
    return new DeletedEntitiesServiceImpl(deletedEntitiesDatabaseService, subscriptionsService);
  }

  @ProxyIgnore
  @GenIgnore
  static DeletedEntitiesService createProxy(Vertx vertx) {
    return new DeletedEntitiesServiceVertxEBProxy(vertx, DELETED_ENTITIES_ADDRESS);
  }

  Future<List<DeletedEntity>> getEntities(JsonObject queryOptions);

  Future<Void> entitiesDeleted(List<String> ids, ResourceType resourceType);
}

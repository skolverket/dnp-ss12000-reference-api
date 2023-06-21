package se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.impl;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.ResourceType;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.DeletedEntitiesService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.database.DeletedEntitiesDatabaseService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.model.DeletedEntity;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.SubscriptionsService;

import java.util.List;


@Slf4j
public class DeletedEntitiesServiceImpl implements DeletedEntitiesService {

  private final DeletedEntitiesDatabaseService deletedEntitiesDatabaseService;
  private final SubscriptionsService subscriptionsService;

  public DeletedEntitiesServiceImpl(DeletedEntitiesDatabaseService deletedEntitiesDatabaseService, SubscriptionsService subscriptionsService) {
    this.deletedEntitiesDatabaseService = deletedEntitiesDatabaseService;
    this.subscriptionsService = subscriptionsService;
  }

  @Override
  public Future<Void> entitiesDeleted(List<String> ids, ResourceType resourceType) {
    return deletedEntitiesDatabaseService.insertDeletedEntities(ids, resourceType)
      .onSuccess(event -> subscriptionsService.dataChanged(resourceType, true));
  }

  @Override
  public Future<List<DeletedEntity>> getEntities(JsonObject queryOptions) {
    return deletedEntitiesDatabaseService.getEntities(queryOptions);
  }
}

package se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.database;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.ResourceType;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.model.DeletedEntity;
import java.util.List;


public interface DeletedEntitiesDatabaseService {

  Future<Void> insertDeletedEntities(List<String> ids, ResourceType resourceType);
  Future<List<DeletedEntity>> getEntities(JsonObject queryOptions);
}

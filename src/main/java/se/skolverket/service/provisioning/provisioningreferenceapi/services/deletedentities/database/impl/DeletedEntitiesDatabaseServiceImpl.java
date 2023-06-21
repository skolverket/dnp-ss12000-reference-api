package se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.database.impl;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.core.CompositeFuture;
import io.vertx.ext.mongo.MongoClient;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.DatabaseServiceHelper;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.ResourceType;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.database.DeletedEntitiesDatabaseService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.model.DeletedEntity;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.BsonConverterHelper.timestampToObjectId;
import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.Constants.*;


@Slf4j
public class DeletedEntitiesDatabaseServiceImpl implements DeletedEntitiesDatabaseService {
  private final MongoClient mongoClient;
  private static final String COLLECTION_NAME = "deletedEntities";

  public DeletedEntitiesDatabaseServiceImpl(MongoClient mongoClient) {
    this.mongoClient = mongoClient;
  }

  public Future<Void> insertDeletedEntities(List<String> ids, ResourceType resourceType) {
    List<Future> insertions = ids.stream().map(deletedEntityId -> {
      log.info("Saving deleted entity {}", deletedEntityId);
      return mongoClient.insert(COLLECTION_NAME, new DeletedEntity(deletedEntityId, resourceType).toBson())
        .compose(id -> {
          log.info("Deleted entity {} saved, got id {}", deletedEntityId, id);
          return Future.succeededFuture();
        });
    }).collect(Collectors.toList());
    return CompositeFuture.all(insertions)
      .compose(c -> Future.succeededFuture());
  }

  @Override
  public Future<List<DeletedEntity>> getEntities(JsonObject queryOptions) {
    int limit = queryOptions.getJsonObject(PT_CURSOR).getInteger(QP_LIMIT);
    JsonObject query = buildQuery(queryOptions);

    return mongoClient.findWithOptions(COLLECTION_NAME, query,
        new FindOptions().setLimit(limit)
          .setSort(new JsonObject().put(BSON_ID, -1)))
      .compose(jsonObjects -> Future.succeededFuture(jsonObjects.stream().map(DeletedEntity::fromBson).collect(Collectors.toList())))
      .recover(DatabaseServiceHelper::errorHandler);
  }

  private JsonObject buildQuery(JsonObject queryOptions) {
    JsonObject query = new JsonObject();
    //Add Pagination to query
    String index = queryOptions.getJsonObject(PT_CURSOR).getString(PT_INDEX);
    if (index != null) {
      query.put(BSON_ID, new JsonObject().put(DB_LESS_THAN, new JsonObject().put(DB_OBJECT_ID, index)));
    }
    //Add Entity & Date-After to query
    if (queryOptions.getJsonObject(PT_REQUEST) != null) {
      JsonObject req = queryOptions.getJsonObject(PT_REQUEST);
      String entities = req.getString(QP_ENTITIES) != null ? req.getString(QP_ENTITIES) : null;
      String dateAfter = req.getString(QP_AFTER) != null ? req.getString(QP_AFTER) : null;

      if (entities != null) {
        addEntityParamsToQuery(query, entities);
      }
      if (dateAfter != null) {
        addDateAfterParamToQuery(query, dateAfter);
      }
    }
    return query;
  }

  private void addDateAfterParamToQuery(JsonObject query, String dateAfter) {
    //After Date Query Param is filtered using auto-generated mongodb-id that has been converted from string timestamp to mongodb objectId.
    JsonObject dateAfterQuery = new JsonObject().put(DB_GREATER_THAN, new JsonObject().put(DB_OBJECT_ID, timestampToObjectId(dateAfter)));
    if (query.getJsonObject(BSON_ID) != null) {
      //if ID/paginationIndex exists - we must replace '_id' with an '$and' array, since 'dateAfter' and 'paginationIndex' are both filtering on '_id'
      JsonObject paginationIndex = query.getJsonObject(BSON_ID);
      query.remove(BSON_ID);
      query.put(DB_AND, new JsonArray()
        .add(new JsonObject().put(BSON_ID, paginationIndex))
        .add(new JsonObject().put(BSON_ID, dateAfterQuery)));
    } else {
      query.put(BSON_ID, dateAfterQuery);
    }
  }

  private void addEntityParamsToQuery(JsonObject query, String entities) {
    List<String> entityList = Arrays.stream(entities.split(",")).collect(Collectors.toList());
    //Entities Query Param
    query.put(RESOURCE_TYPE, new JsonObject().put(DB_MATCHES_ANY, new JsonArray(entityList)));
  }
}

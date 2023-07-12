package se.skolverket.service.provisioning.provisioningreferenceapi.common.helper;

import com.mongodb.MongoWriteException;
import com.mongodb.WriteError;
import io.vertx.core.Future;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.BulkOperation;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.serviceproxy.ServiceException;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.DataType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.Constants.*;

@SuppressWarnings("rawtypes")
@Slf4j
public class DatabaseServiceHelper {

  private static final String META_MODIFIED = "meta.modified";
  private static final String META_CREATED = "meta.created";

  private DatabaseServiceHelper() {
  }

  public static JsonObject parseMongoConfig(JsonObject config) {
    return new JsonObject()
      .put("host", config.getString(CONFIG_KEY_MONGO_HOST, CONFIG_KEY_MONGO_HOST_DEFAULT))
      .put("port", config.getInteger(CONFIG_KEY_MONGO_PORT, CONFIG_KEY_MONGO_PORT_DEFAULT));
  }

  public static <T extends DataType> Future<List<String>> insertDataTypes(MongoClient mongoClient, final String collection, List<T> dataTypes) {
    List<String> ids = dataTypes.stream().map(DataType::getId).collect(Collectors.toList());
    try {
      return findDataTypesByIds(mongoClient, collection, ids)
        .compose(list -> {
          if (list.isEmpty()) {
            List<BulkOperation> bulkOperations = dataTypes.stream()
              .map(obj -> BulkOperation.createInsert(addNewMetaObject(obj.toBson())))
              .collect(Collectors.toList());
            return mongoClient.bulkWrite(collection, bulkOperations)
              .compose(mongoClientBulkWriteResult -> Future.succeededFuture(ids))
              .recover(DatabaseServiceHelper::errorHandler);
          } else {
            return Future.failedFuture(
              new ServiceException(
                409,
                "Key conflict.",
                new JsonObject()
                  .put("id", list.stream().map(entries -> entries.getString("_id")).collect(Collectors.toList()))
              )
            );
          }
        });
    } catch (Exception e) {
      log.error("Unexpected DB error.", e);
      return Future.failedFuture(e);
    }
  }

  /**
   * Saves a list of data model instances which extend DataType and updates
   * any existing meta-fields before saving.
   */
  public static <T extends DataType> Future<List<String>> saveDataTypes(MongoClient mongoClient, final String collection, List<T> dataTypes) {
    List<Future<String>> saveObjectFutureList = dataTypes.stream()
      .map(obj -> {
        log.info("Saving {} with id: {}.", obj.getClass().getSimpleName(), obj.getId());
        JsonObject bson = obj.toBson();
        try {
          return mongoClient.findOne(collection, new JsonObject().put(BSON_ID, obj.getId()), new JsonObject().put(META, 1))
            .compose(fetchedJsonObject -> {
              if (fetchedJsonObject != null) {
                updateMetaObject(bson, fetchedJsonObject);
              } else {
                addNewMetaObject(bson);
              }
              return mongoClient.save(collection, bson)
                .compose(s -> {
                  log.info("{} {} saved.", obj.getClass().getSimpleName(), obj.getId());
                  return Future.succeededFuture(Objects.requireNonNullElse(s, bson.getString(BSON_ID)));
                });
            });
        } catch (Exception e) {
          log.error("Unexpected DB error.", e);
          return Future.<String>failedFuture(e);
        }
      })
      .collect(Collectors.toList());

    return collectIds(saveObjectFutureList);
  }

  private static JsonObject addNewMetaObject(JsonObject jsonObject) {
    Instant now = Instant.now();
    return jsonObject.put(META, new JsonObject()
      .put(CREATED, new JsonObject().put(DB_DATE, now))
      .put(MODIFIED, new JsonObject().put(DB_DATE, now)));
  }

  private static JsonObject updateMetaObject(JsonObject bson, JsonObject fetchedJson) {
    String createdDate = Objects.requireNonNullElse(fetchedJson, addNewMetaObject(new JsonObject()))
      .getJsonObject(META).getString(CREATED);
    return bson.put(META, new JsonObject()
      .put(MODIFIED, new JsonObject().put(DB_DATE, Instant.now()))
      .put(CREATED, new JsonObject(createdDate)));
  }

  /**
   * Deletes from the input collection all objects that exist in the database.
   * This delete function will first query the DB for each input data instance
   * to verify the instance exists before issuing a document removal. This is
   * done in order to verify which instances are actually deleted, which is
   * important to keep track (in the deleted entities service) which objects
   * have been removed. Without this initial query, MongoDB only returns the
   * removal 'count', which isn't possible to correlate to instance IDs.
   */
  public static <T extends DataType> Future<List<String>> deleteDataTypes(MongoClient mongoClient, String collection, List<T> dataTypes) {
    log.info("Attempting to delete {} objects from {}.", dataTypes.size(), collection);
    /*
    1. Perform a query to find all documents that match any of the input IDs
    2. Delete the result documents
    3. Return a list of IDs that were actually deleted
     */
    final List<String> inputIds = dataTypes.stream().map(DataType::getId).collect(Collectors.toList());
    JsonObject findQuery = new JsonObject().put(BSON_ID, new JsonObject().put(DB_MATCHES_ANY, inputIds));
    try {
      return mongoClient.find(collection, findQuery)
        .compose(jsonObjects -> {
          log.info("Found {} objects to delete", jsonObjects.size());
          if (jsonObjects.isEmpty()) return Future.succeededFuture(new ArrayList<String>());

          final List<String> deleteIds = jsonObjects.stream().map(o -> o.getString(BSON_ID)).collect(Collectors.toList());
          JsonObject deleteQuery = new JsonObject().put(BSON_ID, new JsonObject().put(DB_MATCHES_ANY, deleteIds));
          return mongoClient.removeDocuments(collection, deleteQuery)
            .compose(r -> Future.succeededFuture(deleteIds));
        })
        .recover(DatabaseServiceHelper::errorHandler);
    } catch (Exception e) {
      log.error("Unexpected DB error.", e);
      return Future.failedFuture(e);
    }
  }

  public static Future<List<String>> collectIds(List<Future<String>> saveObjectsFutureList) {
    return Future.all(saveObjectsFutureList)
      .compose(compositeFuture -> {
        List<String> createdIds = new LinkedList<>();
        for (int i = 0; i < compositeFuture.size(); i++) {
          if (compositeFuture.resultAt(i) != null) {
            log.info("ID: {}", compositeFuture.resultAt(i).toString());
            createdIds.add(compositeFuture.resultAt(i).toString());
          }
        }
        return Future.succeededFuture(createdIds);
      })
      .recover(DatabaseServiceHelper::errorHandler);
  }


  protected static JsonObject mongo11000ErrorToJson(String mongoErrorMessage) {
    String subString = "{ _id: \"";
    try {
      return new JsonObject().put("id", List.of(mongoErrorMessage.substring(mongoErrorMessage.lastIndexOf(subString) + subString.length(), mongoErrorMessage.lastIndexOf("\""))));
    } catch (Exception e) {
      return new JsonObject();
    }
  }

  public static Future errorHandler(Throwable throwable) {
    try {
      if (throwable instanceof MongoWriteException) {
        WriteError writeError = ((MongoWriteException) throwable).getError();
        switch (writeError.getCode()) {
          case 11000:
            return Future.failedFuture(new ServiceException(409, "Key conflict.", mongo11000ErrorToJson(writeError.getMessage())));
          default:
            log.error("Database error. Error message: {}", writeError.getMessage());
            return Future.failedFuture(new ServiceException(500, "Unknown database error."));
        }
      } else {
        log.error("Unknown database error.", throwable);
        return Future.failedFuture(new ServiceException(500, "Unknown database error."));
      }
    } catch (Exception e) {
      log.error("Unknown error occurred.", e);
      return Future.failedFuture(new ServiceException(500, "Unknown error occurred."));
    }

  }

  public static Future<List<JsonObject>> findDataTypesByIds(MongoClient mongoClient, final String collection, final List<String> ids) {
    JsonObject query = new JsonObject().put(BSON_ID, new JsonObject().put(DB_MATCHES_ANY, ids));
    try {
      return mongoClient.find(collection, query).recover(DatabaseServiceHelper::errorHandler);
    } catch (Exception e) {
      log.error("Unexpected DB error.", e);
      return Future.failedFuture(e);
    }
  }

  public static Future<List<JsonObject>> findDataTypes(MongoClient mongoClient, final String collection, final JsonObject queryOptions) {
    try {
      JsonObject cursorOptions = queryOptions.getJsonObject(PT_CURSOR);

      //Pagination - range query
      JsonObject query = cursorOptions.containsKey(PT_INDEX) ?
        new JsonObject().put(BSON_ID, new JsonObject().put(DB_LESS_THAN, cursorOptions.getString(PT_INDEX))) : new JsonObject();

      JsonObject reqOptions = queryOptions.getJsonObject(PT_REQUEST);
      //Meta.created.*
      addMetaDateRangesToQuery(reqOptions, query, META_CREATED, QP_META_CREATED_BEFORE, QP_META_CREATED_AFTER);
      //Meta.modified.*
      addMetaDateRangesToQuery(reqOptions, query, META_MODIFIED, QP_META_MODIFIED_BEFORE, QP_META_MODIFIED_AFTER);

      //limit - default is (-1)
      int limit = cursorOptions.getInteger(QP_LIMIT);
      return mongoClient.findWithOptions(collection, query, new FindOptions()
          .setLimit(limit)
          //Sort is needed for correct pagination - descending order is (-1)
          .setSort(new JsonObject().put(BSON_ID, -1)))
        .recover(DatabaseServiceHelper::errorHandler);
    } catch (Exception e) {
      log.error("Unexpected DB error.", e);
      return Future.failedFuture(e);
    }
  }

  private static JsonObject addMetaDateRangesToQuery(JsonObject reqQueryOptions, JsonObject query, String metaType,
                                                     String metaBeforeKey, String metaAfterKey) {
    JsonObject metaObjectQuery = new JsonObject();

    if (reqQueryOptions.containsKey(metaBeforeKey)) {
      //date <=
      metaObjectQuery.put(DB_LESS_THAN_OR_EQUALS, new JsonObject().put(DB_DATE, reqQueryOptions.getString(metaBeforeKey)));
    }
    if (reqQueryOptions.containsKey(metaAfterKey)) {
      //date >
      metaObjectQuery.put(DB_GREATER_THAN, new JsonObject().put(DB_DATE, reqQueryOptions.getString(metaAfterKey)));
    }
    if (!metaObjectQuery.isEmpty()) {
      query.put(metaType, metaObjectQuery);
    }
    return metaObjectQuery;
  }
}

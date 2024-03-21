package se.skolverket.service.provisioning.provisioningreferenceapi.services.logging.database.impl;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.AbstractMetaDatabaseService;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.DatabaseServiceHelper;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.logging.database.LoggingDatabaseService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.logging.model.Log;

import java.util.List;
import java.util.stream.Collectors;

import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.Constants.*;

@Slf4j
public class LoggingDatabaseServiceImpl extends AbstractMetaDatabaseService implements LoggingDatabaseService  {

  private final String COLLECTION_NAME = "logs";

  private final MongoClient mongoClient;

  public LoggingDatabaseServiceImpl(MongoClient mongoClient) {
    this.mongoClient = mongoClient;
  }

  @Override
  public Future<String> insertLog(Log logObj) {
    log.info("Saving {}.", logObj.getClass().getSimpleName());
    return mongoClient.insert(COLLECTION_NAME, logObj.toBson())
      .compose(s -> {
        log.info("{} saved, with id {}.", logObj.getClass().getSimpleName(), s);
        return Future.succeededFuture(s);
      })
      .recover(DatabaseServiceHelper::errorHandler);
  }

  @Override
  public Future<List<Log>> findLogs(JsonObject queryOptions) {
    JsonObject query = buildQuery(queryOptions);
    return mongoClient.findWithOptions(COLLECTION_NAME, query,
        new FindOptions().setSort(new JsonObject().put(BSON_ID, -1)))
      .compose(jsonObjects -> Future.succeededFuture(jsonObjects.stream().map(Log::fromBson).collect(Collectors.toList())))
      .recover(DatabaseServiceHelper::errorHandler);
  }


}

package se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.database.impl;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.DatabaseServiceHelper;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.database.DutiesDatabaseService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.model.Duty;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class DutiesDatabaseServiceImpl implements DutiesDatabaseService {

  private final MongoClient mongoClient;

  private static final String COLLECTION_NAME = "duties";

  public DutiesDatabaseServiceImpl(MongoClient mongoClient) {
    this.mongoClient = mongoClient;
  }

  @Override
  public Future<List<String>> insertDuties(List<Duty> duties) {
    return DatabaseServiceHelper.insertDataTypes(mongoClient, COLLECTION_NAME, duties);
  }

  @Override
  public Future<List<String>> saveDuties(List<Duty> duties) {
    return DatabaseServiceHelper.saveDataTypes(mongoClient, COLLECTION_NAME, duties);
  }

  @Override
  public Future<List<String>> deleteDuties(List<Duty> duties) {
    return DatabaseServiceHelper.deleteDataTypes(mongoClient, COLLECTION_NAME, duties);
  }

  @Override
  public Future<List<Duty>> findDuties(JsonObject queryParams) {
    return DatabaseServiceHelper.findDataTypes(mongoClient, COLLECTION_NAME, queryParams)
      .compose(jsonObjects -> Future.succeededFuture(jsonObjects.stream().map(Duty::fromBson).collect(Collectors.toList())));
  }

  @Override
  public Future<List<Duty>> findDutiesByDutyIds(List<String> dutyIds) {
    return DatabaseServiceHelper.findDataTypesByIds(mongoClient, COLLECTION_NAME, dutyIds)
      .compose(jsonObjects -> Future.succeededFuture(jsonObjects.stream().map(Duty::fromBson).collect(Collectors.toList())));
  }
}

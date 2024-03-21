package se.skolverket.service.provisioning.provisioningreferenceapi.services.organisations.database.impl;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.mongo.MongoClient;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.DatabaseServiceHelper;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.organisations.database.OrganisationsDatabaseService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.organisations.model.Organisation;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
public class OrganisationsDatabaseServiceImpl implements OrganisationsDatabaseService {

  private static final String COLLECTION_NAME = "organisations";
  private final MongoClient mongoClient;

  public OrganisationsDatabaseServiceImpl(MongoClient mongoClient) {
    this.mongoClient = mongoClient;
  }

  @Override
  public Future<List<String>> saveOrganisations(List<Organisation> organisations) {
    return DatabaseServiceHelper.saveDataTypes(mongoClient, COLLECTION_NAME, organisations);
  }

  @Override
  public Future<List<String>> insertOrganisations(List<Organisation> organisations) {
    return DatabaseServiceHelper.insertDataTypes(mongoClient, COLLECTION_NAME, organisations);
  }

  @Override
  public Future<List<String>> deleteOrganisations(List<Organisation> organisations) {
    return DatabaseServiceHelper.deleteDataTypes(mongoClient, COLLECTION_NAME, organisations);
  }

  @Override
  public Future<List<Organisation>> findOrganisations(JsonObject queryOptions) {
    try {
      return DatabaseServiceHelper.findDataTypes(mongoClient, COLLECTION_NAME, queryOptions)
        .compose(jsonObjects -> Future.succeededFuture(jsonObjects.stream().map(Organisation::fromBson).collect(Collectors.toList())));
    } catch (Exception e) {
      log.error("Unexpected error.", e);
      return Future.failedFuture(e);
    }
  }

  @Override
  public Future<ReadStream<JsonObject>> findOrganisationsStream(JsonObject queryOptions) {
    try {
      return DatabaseServiceHelper.findDataTypesStream(mongoClient, COLLECTION_NAME, queryOptions);
    } catch (Exception e) {
      log.error("Unexpected error.", e);
      return Future.failedFuture(e);
    }
  }

  @Override
  public Future<List<Organisation>> findOrganisationsByIds(List<String> ids) {
    return DatabaseServiceHelper.findDataTypesByIds(mongoClient, COLLECTION_NAME, ids)
      .compose(jsonObjects -> Future.succeededFuture(jsonObjects.stream().map(Organisation::fromBson).collect(Collectors.toList())));
  }

}

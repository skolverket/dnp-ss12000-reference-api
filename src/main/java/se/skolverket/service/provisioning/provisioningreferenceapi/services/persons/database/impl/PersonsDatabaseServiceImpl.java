package se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.database.impl;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.mongo.MongoClient;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.DatabaseServiceHelper;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.database.PersonsDatabaseService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.model.Person;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
public class PersonsDatabaseServiceImpl implements PersonsDatabaseService {

  private static final String COLLECTION_NAME = "persons";
  private final MongoClient mongoClient;

  public PersonsDatabaseServiceImpl(MongoClient mongoClient) {
    this.mongoClient = mongoClient;
  }

  @Override
  public Future<List<Person>> findPersons(JsonObject queryOptions) {
    try {
      return DatabaseServiceHelper.findDataTypes(mongoClient, COLLECTION_NAME, queryOptions)
        .compose(jsonObjects -> Future.succeededFuture(jsonObjects.stream().map(Person::fromBson).collect(Collectors.toList())));
    } catch (Exception e) {
      log.error("Unexpected error.", e);
      return Future.failedFuture(e);
    }
  }

  @Override
  public Future<ReadStream<JsonObject>> findPersonsStream(JsonObject queryOptions) {
    try {
      return DatabaseServiceHelper.findDataTypesStream(mongoClient, COLLECTION_NAME, queryOptions);
    } catch (Exception e) {
      log.error("Unexpected error.", e);
      return Future.failedFuture(e);
    }
  }

  @Override
  public Future<List<Person>> findPersonsByPersonIds(List<String> personIds) {
    return DatabaseServiceHelper.findDataTypesByIds(mongoClient, COLLECTION_NAME, personIds)
      .compose(jsonObjects -> Future.succeededFuture(jsonObjects.stream().map(Person::fromBson).collect(Collectors.toList())));
  }


  @Override
  public Future<List<Person>> getPersonsByEPPN(List<String> eppnList) {
    JsonObject query = new JsonObject()
      .put("eduPersonPrincipalNames", new JsonObject()
        .put("$in", eppnList)
      );
    return mongoClient.find(COLLECTION_NAME, query)
      .compose(jsonObjects -> Future.succeededFuture(jsonObjects.stream().map(Person::fromBson).collect(Collectors.toList())));
  }

  @Override
  public Future<List<String>> savePersons(List<Person> persons) {
    return DatabaseServiceHelper.saveDataTypes(mongoClient, COLLECTION_NAME, persons);
  }

  @Override
  public Future<List<String>> insertPersons(List<Person> persons) {
    return DatabaseServiceHelper.insertDataTypes(mongoClient, COLLECTION_NAME, persons);
  }

  @Override
  public Future<List<String>> deletePersons(List<Person> persons) {
    return DatabaseServiceHelper.deleteDataTypes(mongoClient, COLLECTION_NAME, persons);
  }
}

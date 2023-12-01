package se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.database;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.model.Person;

import java.util.List;

public interface PersonsDatabaseService {


  Future<List<Person>> getPersonsByEPPN(List<String> eppnList);

  Future<List<String>> savePersons(List<Person> persons);

  Future<List<String>> insertPersons(List<Person> persons);

  Future<List<String>> deletePersons(List<Person> persons);

  Future<List<Person>> findPersons(JsonObject queryOptions);

  Future<ReadStream<JsonObject>> findPersonsStream(JsonObject queryOptions);

  Future<List<Person>> findPersonsByPersonIds(List<String> personIds);
}

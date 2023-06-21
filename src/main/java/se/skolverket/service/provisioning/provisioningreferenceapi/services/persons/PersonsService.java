package se.skolverket.service.provisioning.provisioningreferenceapi.services.persons;

import io.vertx.codegen.annotations.*;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.DeletedEntitiesService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.database.PersonsDatabaseService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.impl.PersonsServiceImpl;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.model.Person;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.SubscriptionsService;

import java.util.List;

@ProxyGen
@VertxGen
public interface PersonsService {

  // Factory methods to create an instance and a proxy

  String ADDRESS = "persons-service";

  @ProxyIgnore
  @GenIgnore
  static PersonsService create(PersonsDatabaseService personsDatabaseService,
                               DeletedEntitiesService deletedEntitiesService,
                               SubscriptionsService subscriptionsService) {
    return new PersonsServiceImpl(personsDatabaseService, deletedEntitiesService, subscriptionsService);
  }

  @ProxyIgnore
  @GenIgnore
  static PersonsService createProxy(Vertx vertx) {
    return new PersonsServiceVertxEBProxy(vertx, PersonsService.ADDRESS);
  }

  Future<List<String>> createPersons(List<Person> persons);

  Future<List<String>> updatePersons(List<Person> persons);

  Future<Void> deletePersons(List<Person> persons);

  Future<List<Person>> getPersons(JsonObject queryParams);

  Future<List<Person>> getPersonsByPersonIds(List<String> personIds);
}

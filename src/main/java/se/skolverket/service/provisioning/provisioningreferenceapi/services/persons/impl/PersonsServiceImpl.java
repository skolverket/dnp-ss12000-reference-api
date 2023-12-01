package se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.impl;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.WriteStream;
import io.vertx.serviceproxy.ServiceException;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.StreamingService;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.DataType;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.ResourceType;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.DeletedEntitiesService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.PersonsService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.database.PersonsDatabaseService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.model.Person;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.SubscriptionsService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class PersonsServiceImpl extends StreamingService implements PersonsService {

  private final PersonsDatabaseService personsDatabaseService;
  private final DeletedEntitiesService deletedEntitiesService;
  private final SubscriptionsService subscriptionsService;

  public PersonsServiceImpl(PersonsDatabaseService personsDatabaseService,
                            DeletedEntitiesService deletedEntitiesService,
                            SubscriptionsService subscriptionsService) {
    this.personsDatabaseService = personsDatabaseService;
    this.deletedEntitiesService = deletedEntitiesService;
    this.subscriptionsService = subscriptionsService;
  }

  private static List<String> getEppns(List<Person> people) {
    return people.stream().map(Person::getEduPersonPrincipalNames).flatMap(List::stream).collect(Collectors.toList());
  }

  private static boolean hasDuplicates(List<String> eppns) {
    return Set.copyOf(eppns).size() < eppns.size();
  }

  @Override
  public Future<List<Person>> getPersons(JsonObject queryParams) {
    return personsDatabaseService.findPersons(queryParams);
  }

  /**
   * Get persons with a streamed response. This is more scalable and uses less memory than getPersons for large amounts of data.
   *
   * @param bufferWriteStream A write stream that will receive the streamed data as Json Format `{ "data": [data array]}` (as buffer).
   * @param queryParams       Query parameters for the person query.
   * @return Future that is completed when the stream has ended.
   */
  @Override
  public Future<Void> getStream(WriteStream<Buffer> bufferWriteStream, JsonObject queryParams) {
    return personsDatabaseService.findPersonsStream(queryParams)
      .compose(stream -> streamProcessor(stream, bufferWriteStream));
  }

  /**
   * Update existing people, or create new people if they do not already exist.
   * Input people will be checked for EPPN duplicates as no 2 people may share
   * any EPPN. If no duplicates are found in the input list, the DB is queried
   * for people that match the provided EPPNs. If matches are found, the DB
   * results and the input people list are combined and if the result is again
   * checked for duplicates. If a DB result person has a colliding ID with an
   * input person, the input person takes precedence as that Person instance
   * will be completely replacing the DB person (due to PUT).
   */
  @Override
  public Future<List<String>> updatePersons(List<Person> persons) {
    List<String> inputEppns = getEppns(persons);

    // Check for input duplicates
    if (hasDuplicates(inputEppns)) {
      return Future.failedFuture(
        new ServiceException(409, "EduPersonPrincipalNames conflict in input list of people.")
      );
    }

    return personsDatabaseService.getPersonsByEPPN(inputEppns)
      .compose(dbPeople -> {
        // Create a map over all people associated with the input EPPNs.
        Map<String, List<String>> eppnsPerPerson = new HashMap<>();
        persons.forEach(person -> eppnsPerPerson.put(person.getId(), person.getEduPersonPrincipalNames()));
        dbPeople.forEach(dbPerson -> {
          // Key check since input people will override DB people.
          if (!eppnsPerPerson.containsKey(dbPerson.getId())) {
            eppnsPerPerson.put(dbPerson.getId(), dbPerson.getEduPersonPrincipalNames());
          }
        });

        // Compose resulting EPPNs from storing input people and check if the
        // result would lead to duplicates.
        List<String> allEppns = eppnsPerPerson.values().stream().flatMap(List::stream).collect(Collectors.toList());
        if (hasDuplicates(allEppns)) {
          return Future.failedFuture(
            new ServiceException(409, "EduPersonPrincipalNames conflict in DB.")
          );
        }

        return personsDatabaseService.savePersons(persons)
          .onSuccess(strings -> subscriptionsService.dataChanged(ResourceType.PERSON, false));
      });
  }

  /**
   * Stored the input people in the DB. Input people will be checked for EPPN
   * duplicates as no 2 people may share any EPPN. If no duplicates are found
   * in the input list, the DB is queried for people that match the provided
   * EPPNs. If matches are found, the request to store people is denied.
   */
  @Override
  public Future<List<String>> createPersons(List<Person> persons) {
    List<String> eppns = getEppns(persons);
    if (hasDuplicates(eppns)) {
      return Future.failedFuture(
        new ServiceException(409, "EduPersonPrincipalNames conflict in input list of people.")
      );
    }

    // Check if EPPN exists in DB, fail request for all persons if yes.
    return personsDatabaseService.getPersonsByEPPN(eppns)
      .compose(people -> {
        if (people.isEmpty()) {
          return personsDatabaseService.insertPersons(persons)
            .onSuccess(strings -> subscriptionsService.dataChanged(ResourceType.PERSON, false));
        } else {
          return Future.failedFuture(new ServiceException(409, "EduPersonPrincipalNames conflict in DB.", new JsonObject().put("persons", people.stream().map(DataType::getId).collect(Collectors.toList()))));
        }
      });
  }

  @Override
  public Future<Void> deletePersons(List<Person> persons) {
    return personsDatabaseService.deletePersons(persons)
      .compose(deletedIds -> {
        if (deletedIds.isEmpty()) return Future.succeededFuture();
        return deletedEntitiesService.entitiesDeleted(deletedIds, ResourceType.PERSON);
      });
  }

  @Override
  public Future<List<Person>> getPersonsByPersonIds(List<String> personIds) {
    return personsDatabaseService.findPersonsByPersonIds(personIds);
  }
}

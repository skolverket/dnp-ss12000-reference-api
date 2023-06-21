package se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.impl;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceException;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.ResourceType;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.DeletedEntitiesService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.DutiesService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.database.DutiesDatabaseService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.model.Duty;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.PersonsService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.SubscriptionsService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DutiesServiceImpl implements DutiesService {

  private final DutiesDatabaseService dutiesDatabaseService;
  private final DeletedEntitiesService deletedEntitiesService;
  private final PersonsService personsService;

  private final SubscriptionsService subscriptionsService;

  public DutiesServiceImpl(DutiesDatabaseService dutiesDatabaseService, DeletedEntitiesService deletedEntitiesService,
                           PersonsService personsService, SubscriptionsService subscriptionsService) {
    this.dutiesDatabaseService = dutiesDatabaseService;
    this.deletedEntitiesService = deletedEntitiesService;
    this.personsService = personsService;
    this.subscriptionsService = subscriptionsService;
  }

  @Override
  public Future<List<String>> createDuties(List<Duty> duties) {
    List<String> personIds = new ArrayList<>(collectPersonRefIds(duties));
    return personsService.getPersonsByPersonIds(personIds).compose(persons -> {
      if (personIds.size() == persons.size()) {
        return dutiesDatabaseService.insertDuties(duties)
          .onSuccess(event -> subscriptionsService.dataChanged(ResourceType.DUTY, false));
      }
      return Future.failedFuture(new ServiceException(400, "Person reference does not exist."));
    });
  }

  @Override
  public Future<List<String>> updateDuties(List<Duty> duties) {
    List<String> personIds = new ArrayList<>(collectPersonRefIds(duties));
    return personsService.getPersonsByPersonIds(personIds).compose(persons -> {
      if (personIds.size() == persons.size()) {
        return dutiesDatabaseService.saveDuties(duties)
          .onSuccess(event -> subscriptionsService.dataChanged(ResourceType.DUTY, false));
      }
      return Future.failedFuture(new ServiceException(400, "Person reference does not exist."));
    });
  }

  @Override
  public Future<Void> deleteDuties(List<Duty> duties) {
    return dutiesDatabaseService.deleteDuties(duties)
      .compose(deletedIds -> {
        if (deletedIds.isEmpty()) return Future.succeededFuture();
        return deletedEntitiesService.entitiesDeleted(deletedIds, ResourceType.DUTY);
      });
  }

  @Override
  public Future<List<Duty>> getDuties(JsonObject queryParams) {
    return dutiesDatabaseService.findDuties(queryParams);
  }

  @Override
  public Future<List<Duty>> getDutiesByDutyIds(List<String> dutyIds) {
    return dutiesDatabaseService.findDutiesByDutyIds(dutyIds);
  }

  private Set<String> collectPersonRefIds(List<Duty> duties) {
    return duties.stream().map(duty -> duty.getPerson().getId()).collect(Collectors.toSet());
  }
}

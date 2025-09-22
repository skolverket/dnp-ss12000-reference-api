package se.skolverket.service.provisioning.provisioningreferenceapi.services.organisations.impl;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.WriteStream;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.StreamingService;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.ResourceType;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.DeletedEntitiesService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.organisations.OrganisationsService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.organisations.database.OrganisationsDatabaseService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.organisations.model.Organisation;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.SubscriptionsService;

import java.util.List;

@Slf4j
public class OrganisationsServiceImpl extends StreamingService implements OrganisationsService {

  private final OrganisationsDatabaseService organisationsDatabaseService;
  private final DeletedEntitiesService deletedEntitiesService;
  private final SubscriptionsService subscriptionsService;

  public OrganisationsServiceImpl(OrganisationsDatabaseService organisationsDatabaseService, DeletedEntitiesService deletedEntitiesService, SubscriptionsService subscriptionsService) {
    this.organisationsDatabaseService = organisationsDatabaseService;
    this.deletedEntitiesService = deletedEntitiesService;
    this.subscriptionsService = subscriptionsService;
  }

  @Override
  public Future<Void> getStream(WriteStream<Buffer> bufferWriteStream, JsonObject queryParams) {
    return organisationsDatabaseService.findOrganisationsStream(queryParams)
      .compose(stream -> streamProcessor(stream, bufferWriteStream, queryParams));
  }

  @Override
  public Future<List<String>> createOrganisations(List<Organisation> organisations) {
    return organisationsDatabaseService.insertOrganisations(organisations)
      .compose(strings -> {
        subscriptionsService.dataChanged(ResourceType.ORGANISATION, false);
        return Future.succeededFuture(strings);
      });
  }

  @Override
  public Future<List<String>> updateOrganisations(List<Organisation> organisations) {
    return organisationsDatabaseService.saveOrganisations(organisations)
      .compose(strings -> {
        subscriptionsService.dataChanged(ResourceType.ORGANISATION, false);
        return Future.succeededFuture(strings);
      });
  }

  @Override
  public Future<Void> deleteOrganisations(List<Organisation> organisations) {
    return organisationsDatabaseService.deleteOrganisations(organisations)
      .compose(deletedIds -> {
        if (deletedIds.isEmpty()) return Future.succeededFuture();
        return deletedEntitiesService.entitiesDeleted(deletedIds, ResourceType.ORGANISATION);
      });
  }

  @Override
  public Future<List<Organisation>> getOrganisations(JsonObject queryOptions) {
    return organisationsDatabaseService.findOrganisations(queryOptions);
  }

  @Override
  public Future<List<Organisation>> getOrganisationsByIds(List<String> ids) {
    return organisationsDatabaseService.findOrganisationsByIds(ids);
  }
}

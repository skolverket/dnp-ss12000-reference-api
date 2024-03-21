package se.skolverket.service.provisioning.provisioningreferenceapi.services.organisations.database;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.organisations.model.Organisation;

import java.util.List;

public interface OrganisationsDatabaseService {

  Future<List<String>> saveOrganisations(List<Organisation> organisations);

  Future<List<String>> insertOrganisations(List<Organisation> organisations);

  Future<List<String>> deleteOrganisations(List<Organisation> organisations);

  Future<List<Organisation>> findOrganisations(JsonObject queryOptions);

  Future<ReadStream<JsonObject>> findOrganisationsStream(JsonObject queryOptions);

  Future<List<Organisation>> findOrganisationsByIds(List<String> ids);
}

package se.skolverket.service.provisioning.provisioningreferenceapi.services.organisations;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.ProxyIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.GroupsService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.organisations.model.Organisation;

import java.util.List;

@ProxyGen
@VertxGen
public interface OrganisationsService {

  String ADDRESS = "organisations-service";

  @ProxyIgnore
  @GenIgnore
  static OrganisationsService createProxy(Vertx vertx) {
    return new OrganisationsServiceVertxEBProxy(vertx, GroupsService.ADDRESS);
  }

  Future<List<String>> createOrganisations(List<Organisation> organisations);

  Future<List<String>> updateOrganisations(List<Organisation> organisations);

  Future<Void> deleteOrganisations(List<Organisation> organisations);

  Future<List<Organisation>> getOrganisations(JsonObject queryOptions);

  Future<List<Organisation>> getOrganisationsByIds(List<String> ids);
}

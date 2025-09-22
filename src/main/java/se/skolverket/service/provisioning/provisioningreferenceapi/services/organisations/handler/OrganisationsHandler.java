package se.skolverket.service.provisioning.provisioningreferenceapi.services.organisations.handler;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.StreamingService;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.SimpleResponseBody;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.organisations.OrganisationsService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.organisations.model.Organisation;

import java.util.List;

import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.Constants.PP_ID;
import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.HandlerHelper.getBodyAndParse;
import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.RequestHelper.*;

@Slf4j
public class OrganisationsHandler {

  private static final String KEY = "organisations";

  public static Handler<RoutingContext> post(OrganisationsService organisationsService) {
    return routingContext -> {
      List<Organisation> persons = getBodyAndParse(routingContext, Organisation.class, KEY);
      organisationsService.createOrganisations(persons)
        .onFailure(routingContext::fail)
        .onSuccess(strings -> response201Json(routingContext, new SimpleResponseBody(strings)));
    };
  }

  public static Handler<RoutingContext> get(StreamingService streamingService) {
    return routingContext -> {
      HttpServerResponse response = routingContext.response();
      response.setChunked(true);
      try {
        JsonObject queryOptions = parseQueryOptions(routingContext.request().params());
        streamingService.getStream(response, queryOptions)
          .onFailure(routingContext::fail)
          .onSuccess(v -> response.end());
      } catch (Exception e) {
        response400Error(routingContext);
      }
    };
  }

  public static Handler<RoutingContext> getByIds(OrganisationsService organisationsService) {
    return routingContext -> {
      String id = routingContext.pathParam(PP_ID);
      organisationsService.getOrganisationsByIds(List.of(id))
        .onFailure(routingContext::fail)
        .onSuccess(organisations -> responseSingleResource(routingContext, organisations));
    };
  }

  public static Handler<RoutingContext> put(OrganisationsService organisationsService) {
    return routingContext -> {
      List<Organisation> persons = getBodyAndParse(routingContext, Organisation.class, KEY);
      organisationsService.updateOrganisations(persons)
        .onFailure(routingContext::fail)
        .onSuccess(strings -> {
          if (strings.isEmpty()) {
            response204(routingContext);
          } else {
            response201Json(routingContext, new SimpleResponseBody(strings));
          }
        });
    };
  }

  public static Handler<RoutingContext> delete(OrganisationsService organisationsService) {
    return routingContext -> {
      List<Organisation> persons = getBodyAndParse(routingContext, Organisation.class, KEY);
      organisationsService.deleteOrganisations(persons)
        .onFailure(throwable -> routingContext.fail(500, throwable))
        .onSuccess(v -> response202Json(routingContext, new SimpleResponseBody("message", "Accepted")));
    };
  }
}

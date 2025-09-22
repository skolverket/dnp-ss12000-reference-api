package se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.handler;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.StreamingService;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.SimpleResponseBody;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.DutiesService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.model.Duty;

import java.util.List;

import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.Constants.PP_ID;
import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.HandlerHelper.getBodyAndParse;
import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.RequestHelper.*;

@Slf4j
public class DutiesHandler {

  private static final String KEY = "duties";

  private DutiesHandler() {
  }

  public static Handler<RoutingContext> postDuties(DutiesService dutiesService) {
    return routingContext -> {
      List<Duty> duties = getBodyAndParse(routingContext, Duty.class, KEY);
      dutiesService.createDuties(duties)
        .onFailure(routingContext::fail)
        .onSuccess(strings -> response201Json(routingContext, new SimpleResponseBody(strings)));
    };
  }

  public static Handler<RoutingContext> deleteDuties(DutiesService dutiesService) {
    return routingContext -> {
      List<Duty> duties = getBodyAndParse(routingContext, Duty.class, KEY);
      dutiesService.deleteDuties(duties)
        .onFailure(throwable -> routingContext.fail(500, throwable))
        .onSuccess(v -> response202Json(routingContext, new SimpleResponseBody("message", "Accepted")));
    };
  }

  public static Handler<RoutingContext> putDuties(DutiesService dutiesService) {
    return routingContext -> {
      List<Duty> duties = getBodyAndParse(routingContext, Duty.class, KEY);
      dutiesService.updateDuties(duties)
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

  public static Handler<RoutingContext> getDuties(StreamingService streamingService) {
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

  public static Handler<RoutingContext> getDutiesByDutyIds(DutiesService dutiesService) {
    return routingContext -> {
      String id = routingContext.request().getParam(PP_ID);
      dutiesService.getDutiesByDutyIds(List.of(id))
        .onFailure(routingContext::fail)
        .onSuccess(duties -> responseSingleResource(routingContext, duties));
    };
  }
}

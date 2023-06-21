package se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.handler;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.SimpleResponseBody;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.DutiesService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.model.Duty;

import java.util.List;

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

  public static Handler<RoutingContext> getDuties(DutiesService dutiesService) {
    return routingContext -> {
      JsonObject queryOptions = parseQueryOptions(routingContext.request().params());
      dutiesService.getDuties(queryOptions)
        .onFailure(routingContext::fail)
        .onSuccess(duties -> {
          JsonArray dutyArray = new JsonArray();
          duties.forEach(duty -> dutyArray.add(duty.toJson()));

          buildAndSend200Response(routingContext, queryOptions, dutyArray);
        });
    };
  }
}

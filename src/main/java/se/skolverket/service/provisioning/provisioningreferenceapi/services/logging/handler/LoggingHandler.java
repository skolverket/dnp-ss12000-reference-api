package se.skolverket.service.provisioning.provisioningreferenceapi.services.logging.handler;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.SimpleResponseBody;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.logging.LoggingService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.logging.model.Log;

import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.RequestHelper.*;

@Slf4j
public class LoggingHandler {
  public static Handler<RoutingContext> postLog(LoggingService loggingService) {
    return routingContext -> {
      Log log = routingContext.body().asJsonObject().mapTo(Log.class);

      loggingService.createLog(log)
        .onFailure(routingContext::fail)
        .onSuccess(s -> response201Json(routingContext, new SimpleResponseBody("message", "created")));
    };
  }

  private LoggingHandler() {
  }

  public static Handler<RoutingContext> getLogs(LoggingService loggingService) {
    return routingContext -> {
      JsonObject queryOptions = parseQueryOptions(routingContext.request().params());
      loggingService.getLogs(queryOptions).onFailure(routingContext::fail)
        .onSuccess(logs -> {
          JsonArray logsArray = new JsonArray();
          logs.forEach(logObj -> logsArray.add(logObj.toJson()));

          response200Json(routingContext, new JsonObject().put("logs", logsArray));
        });
    };
  }
}

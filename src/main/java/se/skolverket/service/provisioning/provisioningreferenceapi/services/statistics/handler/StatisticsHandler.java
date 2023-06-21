package se.skolverket.service.provisioning.provisioningreferenceapi.services.statistics.handler;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.SimpleResponseBody;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.statistics.StatisticsService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.statistics.model.StatisticsEntry;

import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.RequestHelper.*;

@Slf4j
public class StatisticsHandler {
  public static Handler<RoutingContext> postStatisticsEntry(StatisticsService statisticsService) {
    return routingContext -> {
      StatisticsEntry statisticsEntry = routingContext.body().asJsonObject().mapTo(StatisticsEntry.class);

      statisticsService.createStatisticsEntry(statisticsEntry)
        .onFailure(routingContext::fail)
        .onSuccess(s -> response201Json(routingContext, new SimpleResponseBody("message", "created")));
    };
  }

  public static Handler<RoutingContext> getStatistics(StatisticsService statisticsService) {
    return routingContext -> {
      JsonObject queryOptions = parseQueryOptions(routingContext.request().params());
      statisticsService.getStatisticsEntries(queryOptions).onFailure(routingContext::fail)
        .onSuccess(logs -> {
          JsonArray logsArray = new JsonArray();
          logs.forEach(logObj -> logsArray.add(logObj.toJson()));

          response200Json(routingContext, new JsonObject().put("statistics", logsArray));
        });
    };
  }

  private StatisticsHandler() {}
}

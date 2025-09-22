package se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.handler;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.StreamingService;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.SimpleResponseBody;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.ActivitiesService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.model.Activity;

import java.util.List;

import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.Constants.PP_ID;
import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.HandlerHelper.getBodyAndParse;
import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.RequestHelper.*;

public class ActivitiesHandler {

  private static final String JSON_KEY = "activities";

  public static Handler<RoutingContext> postActivities(ActivitiesService activitiesService) {
    return routingContext -> {
      List<Activity> activities = getBodyAndParse(routingContext, Activity.class, JSON_KEY);
      activitiesService.createActivities(activities)
        .onFailure(routingContext::fail)
        .onSuccess(strings -> response201Json(routingContext, new SimpleResponseBody(strings)));
    };
  }


  public static Handler<RoutingContext> getActivities(StreamingService streamingService) {
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

  public static Handler<RoutingContext> getActivityByActivityIds(ActivitiesService activitiesService) {
    return routingContext -> {
      String id = routingContext.request().getParam(PP_ID);
      activitiesService.getActivitiesByActivityIds(List.of(id))
        .onFailure(routingContext::fail)
        .onSuccess(activities -> responseSingleResource(routingContext, activities));
    };
  }

  public static Handler<RoutingContext> putActivities(ActivitiesService activitiesService) {
    return routingContext -> {
      List<Activity> activities = getBodyAndParse(routingContext, Activity.class, JSON_KEY);
      activitiesService.updateActivities(activities)
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

  public static Handler<RoutingContext> deleteActivities(ActivitiesService activitiesService) {
    return routingContext -> {
      List<Activity> activities = getBodyAndParse(routingContext, Activity.class, JSON_KEY);
      activitiesService.deleteActivities(activities)
        .onFailure(throwable -> routingContext.fail(500, throwable))
        .onSuccess(v -> response202Json(routingContext, new SimpleResponseBody("message", "Accepted")));
    };
  }
}

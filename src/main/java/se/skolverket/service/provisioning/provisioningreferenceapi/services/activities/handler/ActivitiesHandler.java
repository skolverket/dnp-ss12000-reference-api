package se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.handler;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.SimpleResponseBody;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.ActivitiesService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.model.Activity;

import java.util.List;

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


  public static Handler<RoutingContext> getActivities(ActivitiesService activitiesService) {
    return routingContext -> {
      JsonObject queryOptions = parseQueryOptions(routingContext.request().params());
      activitiesService.findActivities(queryOptions)
        .onFailure(routingContext::fail)
        .onSuccess(activities -> {
          JsonArray activitiesArray = new JsonArray();
          activities.forEach(activity -> activitiesArray.add(activity.toJson()));

          buildAndSend200Response(routingContext, queryOptions, activitiesArray);
        });
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

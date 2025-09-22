package se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.handler;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.StreamingService;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.SimpleResponseBody;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.GroupsService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.model.Group;

import java.util.List;

import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.Constants.PP_ID;
import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.HandlerHelper.getBodyAndParse;
import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.RequestHelper.*;

@Slf4j
public class GroupsHandler {

  private static final String KEY = "groups";

  private GroupsHandler() {
  }

  public static Handler<RoutingContext> postGroups(GroupsService groupsService) {
    return routingContext -> {
      List<Group> groups = getBodyAndParse(routingContext, Group.class, KEY);

      groupsService.createGroups(groups)
        .onFailure(throwable -> routingContext.fail(500, throwable))
        .onSuccess(strings -> response201Json(routingContext, new SimpleResponseBody(strings)));
    };
  }

  public static Handler<RoutingContext> putGroups(GroupsService groupsService) {
    return routingContext -> {
      List<Group> groups = getBodyAndParse(routingContext, Group.class, KEY);
      groupsService.updateGroups(groups)
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

  public static Handler<RoutingContext> deleteGroups(GroupsService groupsService) {
    return routingContext -> {
      List<Group> groups = getBodyAndParse(routingContext, Group.class, KEY);
      groupsService.deleteGroups(groups)
        .onFailure(throwable -> routingContext.fail(500, throwable))
        .onSuccess(v -> response202Json(routingContext, new SimpleResponseBody("message", "Accepted")));
    };
  }

  public static Handler<RoutingContext> getGroups(StreamingService streamingService) {
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

  public static Handler<RoutingContext> getGroupByGroupIds(GroupsService groupsService) {
    return routingContext -> {
      String id = routingContext.request().getParam(PP_ID);
      groupsService.getGroupsByGroupId(List.of(id))
        .onFailure(routingContext::fail)
        .onSuccess(groups -> responseSingleResource(routingContext, groups));
    };
  }
}

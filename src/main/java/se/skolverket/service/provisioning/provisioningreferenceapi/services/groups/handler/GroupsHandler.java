package se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.handler;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.SimpleResponseBody;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.GroupsService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.model.Group;

import java.util.List;

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

  public static Handler<RoutingContext> getGroups(GroupsService groupsService) {
    return routingContext -> {
      JsonObject queryOptions = parseQueryOptions(routingContext.request().params());
      groupsService.getGroups(queryOptions)
        .onFailure(routingContext::fail)
        .onSuccess(groups -> {
          JsonArray groupsArray = new JsonArray();
          groups.forEach(person -> groupsArray.add(person.toJson()));

          buildAndSend200Response(routingContext, queryOptions, groupsArray);
        });
    };
  }
}

package se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.handler;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.DeletedEntitiesService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.model.DeletedEntitiesResponse;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.model.DeletedEntity;

import java.util.List;

import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.Constants.*;
import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.RequestHelper.*;

@Slf4j
public class DeletedEntitiesHandler {

  private DeletedEntitiesHandler() {
  }

  public static Handler<RoutingContext> getDeletedEntities(DeletedEntitiesService deletedEntitiesService) {
    return routingContext -> {
      JsonObject queryOptions = parseQueryOptions(routingContext.request().params());
      deletedEntitiesService.getEntities(queryOptions)
        .onFailure(routingContext::fail)
        .onSuccess(entities -> {
          DeletedEntitiesResponse response = new DeletedEntitiesResponse(entities);
          //last index in array is next pageToken
          int lastPosition = entities.size() - 1;
          //omit pageToken if there are no more entries or query-limit exceeds entries.
          int limit = queryOptions.getJsonObject(PT_CURSOR).getInteger(QP_LIMIT);
          if (isLastPage(entities, lastPosition, limit)) {
            response200Json(routingContext, new JsonObject().put("data", response));
          } else {
            String lastIndex = entities.get(lastPosition).getId();
            String nextPageToken = buildNextPageToken(queryOptions, lastIndex);
            response200Json(routingContext, new JsonObject().put("data", response).put(QP_PAGE_TOKEN, nextPageToken));
          }
        });
    };
  }

  public static boolean isLastPage(List<DeletedEntity> entities, int lastPosition, int limit) {
    return lastPosition < 0 || (limit > entities.size() || limit < 1);
  }
}

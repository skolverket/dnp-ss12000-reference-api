package se.skolverket.service.provisioning.provisioningreferenceapi.common.helper;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Base64;

import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.HandlerHelper.isLastPage;
import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.Constants.*;

@Slf4j
public class RequestHelper {

  private static final Base64.Decoder decoder = Base64.getDecoder();
  private static final Base64.Encoder encoder = Base64.getEncoder();

  private RequestHelper() {}

  public static boolean queryParamsValid(MultiMap queryParams) {
    /*
     * Valid if:
     * pageToken isn't set
     * OR
     * if the pageToken is set, then none of the date-parameters are set as well.
     */
    return !queryParams.contains(QP_PAGE_TOKEN) ||
      !(queryParams.contains(QP_META_MODIFIED_AFTER) ||
        queryParams.contains(QP_META_MODIFIED_BEFORE) ||
        queryParams.contains(QP_META_CREATED_BEFORE) ||
        queryParams.contains(QP_AFTER) ||
        queryParams.contains(QP_ENTITIES) ||
        queryParams.contains(QP_META_CREATED_AFTER));
  }

  public static JsonObject parseQueryOptions(MultiMap queryParams) {
    JsonObject queryOptions = queryParams.contains(QP_PAGE_TOKEN) ?
      decodePageToken(queryParams.get(QP_PAGE_TOKEN)) : new JsonObject()
      .put(PT_REQUEST, new JsonObject())
      // -1 == no limit
      .put(PT_CURSOR, new JsonObject().put(QP_LIMIT, -1));

    queryParams.forEach((k, v) -> {
      switch (k) {
        case QP_PAGE_TOKEN:
          // ignore
          break;
        case QP_META_CREATED_BEFORE:
          queryOptions.getJsonObject(PT_REQUEST)
            .put(QP_META_CREATED_BEFORE, queryParams.get(QP_META_CREATED_BEFORE));
          break;
        case QP_META_CREATED_AFTER:
          queryOptions.getJsonObject(PT_REQUEST)
            .put(QP_META_CREATED_AFTER, queryParams.get(QP_META_CREATED_AFTER));
          break;
        case QP_META_MODIFIED_BEFORE:
          queryOptions.getJsonObject(PT_REQUEST)
            .put(QP_META_MODIFIED_BEFORE, queryParams.get(QP_META_MODIFIED_BEFORE));
          break;
        case QP_META_MODIFIED_AFTER:
          queryOptions.getJsonObject(PT_REQUEST)
            .put(QP_META_MODIFIED_AFTER, queryParams.get(QP_META_MODIFIED_AFTER));
          break;
        case QP_LIMIT:
          queryOptions.getJsonObject(PT_CURSOR)
            .put(QP_LIMIT, Integer.parseInt(queryParams.get(QP_LIMIT)));
          break;
        case QP_AFTER:
          queryOptions.getJsonObject(PT_REQUEST)
            .put(QP_AFTER, queryParams.get(QP_AFTER));
          break;
        case QP_BEFORE:
          queryOptions.getJsonObject(PT_REQUEST)
            .put(QP_BEFORE, queryParams.get(QP_BEFORE));
          break;
        case QP_ENTITIES:
          queryOptions.getJsonObject(PT_REQUEST)
            .put(QP_ENTITIES, queryParams.get(QP_ENTITIES));
          break;
        default:
          log.info("Got unrecognized query parameter '{}'", k);
          break;
      }
    });
    return queryOptions;
  }

  private static JsonObject decodePageToken(String encodedPageToken) {
    return new JsonObject(Buffer.buffer(decoder.decode(encodedPageToken)));
  }

  public static String buildNextPageToken(JsonObject previousPageToken, String newIndex) {
    previousPageToken.getJsonObject(PT_CURSOR).put(PT_INDEX, newIndex);
    return encoder.encodeToString(previousPageToken.toBuffer().getBytes());
  }

  public static void response200Json(RoutingContext routingContext, JsonObject jsonObject) {
    routingContext
      .response()
      .setStatusCode(200)
      .putHeader("Content-Type", "application/json")
      .end(jsonObject.encode());
  }

  public static void response201Json(RoutingContext routingContext, JsonObject jsonObject) {
    routingContext
      .response()
      .setStatusCode(201)
      .putHeader("Content-Type", "application/json")
      .end(jsonObject.encode());
  }

  public static void response202Json(RoutingContext routingContext, JsonObject jsonObject) {
    routingContext
      .response()
      .setStatusCode(202)
      .putHeader("Content-Type", "application/json")
      .end(jsonObject.encode());
  }

  public static void response204(RoutingContext routingContext) {
    routingContext
      .response()
      .setStatusCode(204)
      .end();
  }

  public static void response400Error(RoutingContext routingContext) {
    routingContext
      .response()
      .putHeader("Content-Type", "application/json")
      .setStatusCode(400)
      .end(getErrorJson(400, "Bad request.").encode());
  }

  public static void response500Error(RoutingContext routingContext) {
    routingContext
      .response()
      .putHeader("Content-Type", "application/json")
      .setStatusCode(500)
      .end(getErrorJson(500, "Internal server error.").encode());
  }

  public static void responseError(RoutingContext routingContext, Integer statusCode, JsonObject errorMessage) {
    routingContext
      .response()
      .setStatusCode(statusCode)
      .putHeader("Content-Type", "application/json")
      .end(errorMessage.encode());
  }

  public static String buildPageToken(JsonObject queryOptions, int responseSize, JsonObject lastObject) {
    //last index in array is next pageToken
    int lastPosition = responseSize - 1;

    //omit pageToken if there are no more entries or query-limit exceeds entries.
    int limit = queryOptions.getJsonObject(PT_CURSOR).getInteger(QP_LIMIT);
    if (isLastPage(responseSize, lastPosition, limit)) {
      return null;
    } else {
      String lastIndex = lastObject.getString(ID);
      return buildNextPageToken(queryOptions, lastIndex);

/*      response200Json(routingContext, new JsonObject()
        .put("data", dataTypeArray)
        .put("pageToken", nextPageToken));*/
    }
  }

  private static JsonObject getErrorJson(Integer code, String message) {
    return new JsonObject()
      .put(
        "error",
        new JsonObject()
          .put("code", code)
          .put("message", message));
  }
}

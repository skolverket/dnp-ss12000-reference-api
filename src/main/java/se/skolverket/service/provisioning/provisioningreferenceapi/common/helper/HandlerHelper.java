package se.skolverket.service.provisioning.provisioningreferenceapi.common.helper;

import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;
import io.vertx.serviceproxy.ServiceException;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.DataType;

import java.util.List;

@Slf4j
public class HandlerHelper {

  private HandlerHelper() {
  }

  public static <T extends DataType> List<T> getBodyAndParse(RoutingContext routingContext, Class<T> clazz, final String key) {
    try {
      JsonArray jsonArray = routingContext.body().asJsonObject().getJsonArray(key);
      return DataType.fromJsonArray(jsonArray, clazz);
    } catch (Exception e) {
      log.warn("Unable to parse body for class {}.", clazz.getName(), e);
      throw new ServiceException(400, "Unable to parse body.");
    }
  }

  public static boolean isLastPage(JsonArray personsArray, int lastPosition, int limit) {
    return lastPosition < 0 || (limit > personsArray.size() || limit < 1);
  }
}

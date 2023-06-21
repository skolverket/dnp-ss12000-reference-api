package se.skolverket.service.provisioning.provisioningreferenceapi.common.model;

import io.vertx.core.json.JsonObject;

import java.util.List;

public class SimpleResponseBody extends JsonObject {

  public SimpleResponseBody(List<String> ids) {
    super.put("ids", ids);
  }

  public SimpleResponseBody(String message, String status) {
    super.put(message, status);
  }
}

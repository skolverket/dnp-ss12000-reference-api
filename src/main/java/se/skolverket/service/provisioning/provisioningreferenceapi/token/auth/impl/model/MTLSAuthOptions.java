package se.skolverket.service.provisioning.provisioningreferenceapi.token.auth.impl.model;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import se.skolverket.service.provisioning.provisioningreferenceapi.token.model.AuthConfig;

import java.util.List;

@Accessors(fluent = true, chain = true)
@Getter
@Setter
public class MTLSAuthOptions {
  private AuthConfig authConfig;

  public MTLSAuthOptions() {

  }

  public JsonObject getRequestBody() {
    return new JsonObject()
      .put("access_token",
        new JsonArray()
          .add(
            new JsonObject()
              .put("flags", List.of("bearer"))
          )
      ).put("client",
        new JsonObject()
          .put("key", authConfig.getClientKey())
      );
  }
}

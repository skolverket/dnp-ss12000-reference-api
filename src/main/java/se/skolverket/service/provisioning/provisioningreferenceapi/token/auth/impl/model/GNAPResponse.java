package se.skolverket.service.provisioning.provisioningreferenceapi.token.auth.impl.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.skolverket.service.provisioning.provisioningreferenceapi.token.model.AccessToken;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
public class GNAPResponse {

  @JsonProperty("access_token")
  private AccessToken accessToken;

  public JsonObject toJson() {
    return JsonObject.mapFrom(this);
  }
}

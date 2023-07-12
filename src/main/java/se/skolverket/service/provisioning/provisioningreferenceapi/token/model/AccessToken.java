package se.skolverket.service.provisioning.provisioningreferenceapi.token.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
public class AccessToken {
  @JsonProperty("value")
  private String value;
  @JsonProperty("expires_in")
  private Long expiresIn;

  public JsonObject toJson() {
    return JsonObject.mapFrom(this);
  }

  @Override
  public String toString() {
    return "AccessToken{" +
      "value='XXXXXXXXX" + '\'' +
      ", expiresIn=" + expiresIn +
      '}';
  }
}

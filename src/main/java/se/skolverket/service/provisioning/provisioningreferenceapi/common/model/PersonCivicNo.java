package se.skolverket.service.provisioning.provisioningreferenceapi.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonCivicNo {
  @JsonProperty("value")
  private String value = null;
  @JsonProperty("nationality")
  private String nationality = "SWE";

  public JsonObject toJson() {
    return JsonObject.mapFrom(this);
  }
}

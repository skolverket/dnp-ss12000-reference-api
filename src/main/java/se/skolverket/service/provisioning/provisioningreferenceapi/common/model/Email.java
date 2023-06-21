package se.skolverket.service.provisioning.provisioningreferenceapi.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import lombok.*;

import java.io.Serializable;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Email implements Serializable {
  private static final long serialVersionUID = 1L;
  @JsonProperty("value")
  private String value = null;
  @JsonProperty("type")
  private String type = "Privat";

  public JsonObject toJson() {
    return JsonObject.mapFrom(this);
  }
}

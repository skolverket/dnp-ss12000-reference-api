package se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalIdentifier {
  @JsonProperty("value")
  private String value;

  @JsonProperty("context")
  private String context;

  @JsonProperty("globallyUnique")
  private boolean globallyUnique;

  public JsonObject toJson() {
    return JsonObject.mapFrom(this);
  }
}

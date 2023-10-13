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
public class DutyAssignment {

  @JsonProperty("duty")
  private ObjectReference duty;


  public static DutyAssignment fromBson(JsonObject bson) {
    return bson.mapTo(DutyAssignment.class);
  }

  public JsonObject toBson() {
    return JsonObject.mapFrom(this);
  }
}

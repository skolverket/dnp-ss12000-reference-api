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
public class Enrolment implements Serializable {
  private static final long serialVersionUID = 1L;
  @JsonProperty("enroledAt")
  private SchoolUnitReference enroledAt = null;
  @JsonProperty("schoolYear")
  private Integer schoolYear = null;
  @JsonProperty("schoolType")
  private String schoolType = null;
  @JsonProperty("cancelled")
  private Boolean cancelled = false;
  @JsonProperty("educationCode")
  private String educationCode = null;
  @JsonProperty("programme")
  private ObjectReference programme = null;
  @JsonProperty("specification")
  private String specification = null;

  public JsonObject toJson() {
    return JsonObject.mapFrom(this);
  }
}

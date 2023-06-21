package se.skolverket.service.provisioning.provisioningreferenceapi.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GroupMembership extends DataType implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("id")
  private String id;

  @JsonProperty("person")
  private PersonReference person;

  @JsonProperty("startDate")
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private LocalDate startDate;

  @JsonProperty("endDate")
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private LocalDate endDate;

  public JsonObject toJson() {
    return super.toJson();
  }

  @Override
  public JsonObject toBson() {
    JsonObject jsonObject = JsonObject.mapFrom(this);
    convertLocalDateToMongoDate("startDate", this.startDate, jsonObject);
    if (this.endDate != null) {
      convertLocalDateToMongoDate("endDate", this.endDate, jsonObject);
    } else {
      jsonObject.remove("endDate");
    }
    return jsonObject;
  }
}

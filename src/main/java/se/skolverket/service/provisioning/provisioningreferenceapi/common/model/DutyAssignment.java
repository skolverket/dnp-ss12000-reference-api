package se.skolverket.service.provisioning.provisioningreferenceapi.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import lombok.*;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.BsonConverterHelper;

import java.time.LocalDate;

import static se.skolverket.service.provisioning.provisioningreferenceapi.common.model.DataType.convertLocalDateToMongoDate;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DutyAssignment {

  @JsonProperty("duty")
  private ObjectReference duty;

  @JsonProperty("startDate")
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private LocalDate startDate;

  @JsonProperty("endDate")
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private LocalDate endDate;

  @JsonProperty("grader")
  private boolean grader = false;

  public static DutyAssignment fromBson(JsonObject bson) {
    return BsonConverterHelper.convertStarEndDateToJson(bson).mapTo(DutyAssignment.class);
  }

  public JsonObject toBson() {
    JsonObject jsonObject = JsonObject.mapFrom(this);
    convertLocalDateToMongoDate("startDate", this.startDate, jsonObject);
    convertLocalDateToMongoDate("endDate", this.endDate, jsonObject);
    return jsonObject;
  }
}

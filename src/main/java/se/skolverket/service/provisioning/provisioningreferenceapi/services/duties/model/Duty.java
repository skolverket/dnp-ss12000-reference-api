package se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.*;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.BsonConverterHelper;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.*;

import java.time.LocalDate;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@DataObject
@JsonIgnoreProperties(ignoreUnknown = true)
public class Duty extends DataType {

  @JsonProperty("person")
  private PersonReference person;

  @JsonProperty("dutyAt")
  private OrganisationReference dutyAt;

  @JsonProperty("dutyRole")
  private String dutyRole;

  @JsonProperty("startDate")
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private LocalDate startDate;

  @JsonProperty("endDate")
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private LocalDate endDate;

  public static Duty fromBson(JsonObject bson) {
    Duty duty = (Duty) fromBson(new Duty(), bson);
    BsonConverterHelper.convertStarEndDateToJson(bson);
    duty.person = bson.getJsonObject("person").mapTo(PersonReference.class);
    duty.dutyAt = bson.getJsonObject("dutyAt").mapTo(OrganisationReference.class);
    duty.dutyRole = bson.getString("dutyRole");
    duty.startDate = LocalDate.parse(bson.getString("startDate"));
    duty.endDate = bson.getString("endDate") != null ? LocalDate.parse(bson.getString("endDate")) : null;

    return duty;
  }

  public Duty(JsonObject jsonObject) {
    super(jsonObject);
    this.person = jsonObject.getJsonObject("person") != null ? jsonObject.getJsonObject("person").mapTo(PersonReference.class) : null;
    this.dutyAt = jsonObject.getJsonObject("dutyAt") != null ? jsonObject.getJsonObject("dutyAt").mapTo(OrganisationReference.class) : null;
    this.dutyRole = jsonObject.getString("dutyRole");
    this.startDate = jsonObject.getString("startDate") != null ? LocalDate.parse(jsonObject.getString("startDate")) : null;
    this.endDate = jsonObject.getString("endDate") != null ? LocalDate.parse(jsonObject.getString("endDate")) : null;
  }

  @Override
  public JsonObject toBson() {
    JsonObject jsonObject = super.toBson();
    convertLocalDateToMongoDate("startDate", this.startDate, jsonObject);
    convertLocalDateToMongoDate("endDate", this.endDate, jsonObject);

    return jsonObject;
  }
}

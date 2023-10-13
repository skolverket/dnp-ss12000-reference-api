package se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.*;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.*;

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

  public static Duty fromBson(JsonObject bson) {
    Duty duty = (Duty) fromBson(new Duty(), bson);
    duty.person = bson.getJsonObject("person").mapTo(PersonReference.class);
    duty.dutyAt = bson.getJsonObject("dutyAt").mapTo(OrganisationReference.class);
    duty.dutyRole = bson.getString("dutyRole");

    return duty;
  }

  public Duty(JsonObject jsonObject) {
    super(jsonObject);
    this.person = jsonObject.getJsonObject("person") != null ? jsonObject.getJsonObject("person").mapTo(PersonReference.class) : null;
    this.dutyAt = jsonObject.getJsonObject("dutyAt") != null ? jsonObject.getJsonObject("dutyAt").mapTo(OrganisationReference.class) : null;
    this.dutyRole = jsonObject.getString("dutyRole");
  }

  @Override
  public JsonObject toBson() {
    return super.toBson();
  }
}

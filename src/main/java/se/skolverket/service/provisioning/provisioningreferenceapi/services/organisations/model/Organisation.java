package se.skolverket.service.provisioning.provisioningreferenceapi.services.organisations.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.DataType;

@DataObject
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Organisation extends DataType {
  @JsonProperty("displayName")
  private String displayName;

  @JsonProperty("schoolUnitCode")
  private String schoolUnitCode;

  public Organisation() {
  }

  public static Organisation fromBson(JsonObject bson) {
    return new Organisation(DataType.fromBsonJson(bson));
  }

  public Organisation(JsonObject jsonObject) {
    super(jsonObject);
    this.displayName = jsonObject.getString("displayName");
    this.schoolUnitCode = jsonObject.getString("schoolUnitCode");
  }
}

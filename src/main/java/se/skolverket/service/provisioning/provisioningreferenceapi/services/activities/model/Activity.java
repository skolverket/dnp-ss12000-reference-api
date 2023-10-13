package se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.DataType;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.DutyAssignment;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.ObjectReference;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.OrganisationReference;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@DataObject
@JsonIgnoreProperties(ignoreUnknown = true)
public class Activity extends DataType {

  @JsonProperty("displayName")
  private String displayName;

  @JsonProperty("activityType")
  private String activityType;

  @JsonProperty("groups")
  private List<ObjectReference> groups;

  @JsonProperty("teachers")
  private List<DutyAssignment> teachers;

  @JsonProperty("organisation")
  private OrganisationReference organisation;

  @JsonProperty("parentActivity")
  private ObjectReference parentActivity;

  public Activity(JsonObject jsonObject) {
    super(jsonObject);
    this.displayName = jsonObject.getString("displayName");
    this.activityType = jsonObject.getString("activityType");
    this.groups = jsonObject.getJsonArray("groups") != null ? jsonObject.getJsonArray("groups").stream().map(o -> ((JsonObject) o).mapTo(ObjectReference.class)).collect(Collectors.toList()) : null;
    this.teachers = jsonObject.getJsonArray("teachers") != null ? jsonObject.getJsonArray("teachers").stream().map(o -> ((JsonObject) o).mapTo(DutyAssignment.class)).collect(Collectors.toList()) : null;
    this.organisation = jsonObject.getJsonObject("organisation") != null ? jsonObject.getJsonObject("organisation").mapTo(OrganisationReference.class) : null;
    this.parentActivity = jsonObject.getJsonObject("parentActivity") != null ? jsonObject.getJsonObject("parentActivity").mapTo(ObjectReference.class) : null;
  }

  public static Activity fromBson(JsonObject bson) {
    Activity activity = (Activity) fromBson(new Activity(), bson);
    activity.setDisplayName(bson.getString("displayName"));
    activity.setActivityType(bson.getString("activityType"));
    activity.setGroups(bson.getJsonArray("groups") != null ? bson.getJsonArray("groups").stream().map(o -> ((JsonObject) o).mapTo(ObjectReference.class)).collect(Collectors.toList()) : null);
    activity.setTeachers(bson.getJsonArray("teachers") != null ? bson.getJsonArray("teachers").stream().map(o -> DutyAssignment.fromBson((JsonObject) o)).collect(Collectors.toList()) : null);
    activity.setOrganisation(bson.getJsonObject("organisation") != null ? bson.getJsonObject("organisation").mapTo(OrganisationReference.class) : null);
    activity.setParentActivity(bson.getJsonObject("parentActivity") != null ? bson.getJsonObject("parentActivity").mapTo(ObjectReference.class) : null);
    return activity;
  }

  @Override
  public JsonObject toBson() {
    JsonObject jsonObject = super.toBson();

    if (teachers != null) {
      JsonArray teachersJsonArray = new JsonArray();
      teachers.forEach(dutyAssignment -> teachersJsonArray.add(dutyAssignment.toBson()));
      jsonObject.put("teachers", teachersJsonArray);
    }

    return jsonObject;
  }

}

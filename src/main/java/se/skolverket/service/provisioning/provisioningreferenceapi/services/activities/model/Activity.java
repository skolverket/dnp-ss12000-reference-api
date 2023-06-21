package se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.BsonConverterHelper;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.DataType;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.DutyAssignment;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.ObjectReference;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.OrganisationReference;

import java.time.LocalDate;
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

  @JsonProperty("startDate")
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private LocalDate startDate;

  @JsonProperty("endDate")
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private LocalDate endDate;

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
    this.startDate = jsonObject.getString("startDate") != null ? LocalDate.parse(jsonObject.getString("startDate")) : null;
    this.endDate = jsonObject.getString("endDate") != null ? LocalDate.parse(jsonObject.getString("endDate")) : null;
    this.activityType = jsonObject.getString("activityType");
    this.groups = jsonObject.getJsonArray("groups") != null ? jsonObject.getJsonArray("groups").stream().map(o -> ((JsonObject) o).mapTo(ObjectReference.class)).collect(Collectors.toList()) : null;
    this.teachers = jsonObject.getJsonArray("teachers") != null ? jsonObject.getJsonArray("teachers").stream().map(o -> ((JsonObject) o).mapTo(DutyAssignment.class)).collect(Collectors.toList()) : null;
    this.organisation = jsonObject.getJsonObject("organisation") != null ? jsonObject.getJsonObject("organisation").mapTo(OrganisationReference.class) : null;
    this.parentActivity = jsonObject.getJsonObject("parentActivity") != null ? jsonObject.getJsonObject("parentActivity").mapTo(ObjectReference.class) : null;
  }

  public static Activity fromBson(JsonObject bson) {
    Activity activity = (Activity) fromBson(new Activity(), bson);
    BsonConverterHelper.convertStarEndDateToJson(bson);
    activity.setDisplayName(bson.getString("displayName"));
    activity.setStartDate(bson.getString("startDate") != null ? LocalDate.parse(bson.getString("startDate")) : null);
    activity.setEndDate(bson.getString("endDate") != null ? LocalDate.parse(bson.getString("endDate")) : null);
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
    convertLocalDateToMongoDate("startDate", this.startDate, jsonObject);

    if (endDate != null) {
      convertLocalDateToMongoDate("endDate", this.endDate, jsonObject);
    }

    if (teachers != null) {
      JsonArray teachersJsonArray = new JsonArray();
      teachers.forEach(dutyAssignment -> teachersJsonArray.add(dutyAssignment.toBson()));
      jsonObject.put("teachers", teachersJsonArray);
    }

    return jsonObject;
  }

}

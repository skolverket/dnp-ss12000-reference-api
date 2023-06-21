package se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.*;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.BsonConverterHelper;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@DataObject
@JsonIgnoreProperties(ignoreUnknown = true)
public class Group extends DataType {

  @JsonProperty("displayName")
  private String displayName;

  @JsonProperty("startDate")
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private LocalDate startDate;

  @JsonProperty("endDate")
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private LocalDate endDate;

  @JsonProperty("groupType")
  private GroupType groupType;

  @JsonProperty("schoolType")
  private SchoolType schoolType;

  @JsonProperty("organisation")
  private OrganisationReference organisationReference;

  @JsonProperty("groupMemberships")
  private List<GroupMembership> groupMemberships = new ArrayList<>();

  public Group(JsonObject jsonObject) {
    super(jsonObject);
    this.displayName = jsonObject.getString("displayName");
    this.startDate = jsonObject.getString("startDate") != null ? LocalDate.parse(jsonObject.getString("startDate")) : null;
    this.endDate = jsonObject.getString("endDate") != null ? LocalDate.parse(jsonObject.getString("endDate")) : null;
    this.groupType = jsonObject.getString("groupType") != null ? GroupType.fromString(jsonObject.getString("groupType")) : null;
    this.schoolType = jsonObject.getString("schoolType") != null ? SchoolType.valueOf(jsonObject.getString("schoolType")) : null;
    this.organisationReference = jsonObject.getJsonObject("organisation") != null ? jsonObject.getJsonObject("organisation").mapTo(OrganisationReference.class) : null;
    this.groupMemberships = jsonObject.getJsonArray("groupMemberships") != null ?
      jsonObject.getJsonArray("groupMemberships")
        .stream()
        .map(o -> ((JsonObject)o).mapTo(GroupMembership.class)).collect(Collectors.toList()) :
      new ArrayList<>();
  }

  public static Group fromBson(JsonObject bson) {
    Group group = (Group) fromBson(new Group(), bson);
    BsonConverterHelper.convertStarEndDateToJson(bson);
    BsonConverterHelper.convertStarEndDateInArrayToJson(bson, "groupMemberships");
    group.displayName = bson.getString("displayName");
    group.startDate = bson.getString("startDate") != null ? LocalDate.parse(bson.getString("startDate")) : null;
    group.endDate = bson.getString("endDate") != null ? LocalDate.parse(bson.getString("endDate")) : null;
    group.groupType = bson.getString("groupType") != null ? GroupType.fromString(bson.getString("groupType")) : null;
    group.schoolType = bson.getString("schoolType") != null ? SchoolType.valueOf(bson.getString("schoolType")) : null;
    group.organisationReference = bson.getJsonObject("organisation") != null ? bson.getJsonObject("organisation").mapTo(OrganisationReference.class) : null;
    group.groupMemberships = bson.getJsonArray("groupMemberships") != null ?
      bson.getJsonArray("groupMemberships")
        .stream()
        .map(o -> ((JsonObject)o).mapTo(GroupMembership.class)).collect(Collectors.toList()) :
      new ArrayList<>();

    return group;
  }

  @Override
  public JsonObject toBson() {
    JsonObject jsonObject = super.toBson();
    convertLocalDateToMongoDate("startDate", this.startDate, jsonObject);
    convertLocalDateToMongoDate("endDate", this.endDate, jsonObject);

    JsonArray groupMembershipsJsonArray = new JsonArray();
    groupMemberships.forEach(groupMembership -> groupMembershipsJsonArray.add(groupMembership.toBson()));
    jsonObject.put("groupMemberships", groupMembershipsJsonArray);
    return jsonObject;
  }
}

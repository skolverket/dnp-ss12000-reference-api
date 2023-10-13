package se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.*;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.*;

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
    group.displayName = bson.getString("displayName");
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

    JsonArray groupMembershipsJsonArray = new JsonArray();
    groupMemberships.forEach(groupMembership -> groupMembershipsJsonArray.add(groupMembership.toBson()));
    jsonObject.put("groupMemberships", groupMembershipsJsonArray);
    return jsonObject;
  }
}

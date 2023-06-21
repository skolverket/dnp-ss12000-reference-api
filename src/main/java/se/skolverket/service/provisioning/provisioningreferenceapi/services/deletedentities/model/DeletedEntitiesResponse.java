package se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.*;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.ResourceType;

import java.util.ArrayList;
import java.util.List;

@DataObject
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeletedEntitiesResponse {

  @JsonProperty("activities")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  List<String> activities;

  @JsonProperty("duties")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  List<String> duties;

  @JsonProperty("groups")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  List<String> groups;

  @JsonProperty("persons")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  List<String> persons;

  public DeletedEntitiesResponse(List<DeletedEntity> deletedEntities) {

    deletedEntities.forEach(entity -> {
      if (entity.getResourceType() == ResourceType.ACTIVITY) {
        if(this.activities == null) this.activities = new ArrayList<>();
        this.activities.add(entity.getDeletedEntityId());
      }
      if (entity.getResourceType() == ResourceType.DUTY) {
        if(this.duties == null) this.duties = new ArrayList<>();
        this.duties.add(entity.getDeletedEntityId());
      }
      if (entity.getResourceType() == ResourceType.GROUP) {
        if(this.groups == null) this.groups = new ArrayList<>();
        this.groups.add(entity.getDeletedEntityId());
      }
      if (entity.getResourceType() == ResourceType.PERSON) {
        if(this.persons == null) this.persons = new ArrayList<>();
        this.persons.add(entity.getDeletedEntityId());
      }
    });
  }

  public JsonObject toJson() {
    return JsonObject.mapFrom(this);
  }
}

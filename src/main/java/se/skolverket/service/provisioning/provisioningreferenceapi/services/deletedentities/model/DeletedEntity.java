package se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.*;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.ResourceType;

import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.Constants.*;

@DataObject
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeletedEntity {

  @JsonProperty(ID)
  @JsonAlias(BSON_ID)
  private String id;

  @JsonProperty(DELETED_ENTITY_ID)
  private String deletedEntityId;

  @JsonProperty(RESOURCE_TYPE)
  private ResourceType resourceType;

  public DeletedEntity(JsonObject jsonObject) {
    this.id = jsonObject.getString(ID);
    this.deletedEntityId = jsonObject.getString(DELETED_ENTITY_ID);
    this.resourceType = ResourceType.valueOf(jsonObject.getString(RESOURCE_TYPE).toUpperCase());
  }

  public DeletedEntity(String deletedEntityId, ResourceType resourceType) {
    this.deletedEntityId = deletedEntityId;
    this.resourceType = resourceType;
  }

  public JsonObject toJson() {
    return JsonObject.mapFrom(this);
  }

  public JsonObject toBson() {
    JsonObject jsonObject = JsonObject.mapFrom(this);
    jsonObject.remove(ID);
    return jsonObject;
  }

  public static DeletedEntity fromBson(JsonObject bson) {
    DeletedEntity deletedEntity = new DeletedEntity(bson);
    deletedEntity.id = bson.getString(BSON_ID);
    deletedEntity.deletedEntityId = bson.getString(DELETED_ENTITY_ID);
    deletedEntity.resourceType = ResourceType.valueOf(bson.getString(RESOURCE_TYPE).toUpperCase());

    return deletedEntity;
  }
}

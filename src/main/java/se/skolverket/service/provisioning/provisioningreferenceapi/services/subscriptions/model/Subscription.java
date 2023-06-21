package se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.model;

import com.fasterxml.jackson.annotation.*;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.ResourceType;

import java.util.List;

import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.Constants.*;

@Slf4j
@DataObject
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Subscription {

  @JsonProperty(ID)
  @JsonAlias(BSON_ID)
  private String id;

  @JsonProperty(NAME)
  private String name;

  @JsonProperty(TARGET)
  private String target;

  @JsonProperty(RESOURCE_TYPES)
  private List<ResourceType> resourceTypes;

  public Subscription(JsonObject jsonObject) {
    Subscription subscription = jsonObject.mapTo(Subscription.class);
    id = subscription.getId();
    name = subscription.getName();
    target = subscription.getTarget();
    resourceTypes = subscription.getResourceTypes();
  }

  public static Subscription fromBson(JsonObject jsonObject) {
    return new Subscription(jsonObject);
  }

  public JsonObject toBson() {
    JsonObject jsonObject = toJson();
    jsonObject.remove(ID);
    jsonObject.put(BSON_ID, id);
    return jsonObject;
  }

  public JsonObject toJson() {
    return JsonObject.mapFrom(this);
  }

  @Override
  public String toString() {
    return String.format(
      "Subscription<id: %s, name: %s, target: %s, resourceTypes: %s>",
      id, name, target, resourceTypes
    );
  }
}

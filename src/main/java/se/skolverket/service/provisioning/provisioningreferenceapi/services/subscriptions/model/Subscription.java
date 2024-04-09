package se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.ResourceType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
  private List<SubscriptionResourceType> resourceTypes;

  public Subscription(JsonObject jsonObject) {
    id = jsonObject.getString(ID, jsonObject.getString(BSON_ID));
    name = jsonObject.getString(NAME);
    target = jsonObject.getString(TARGET);

    JsonArray resourceTypesJsonArray = jsonObject.getJsonArray(RESOURCE_TYPES, new JsonArray());
    if (resourceTypesJsonArray != null) {
      resourceTypes = new ArrayList<>(resourceTypesJsonArray.size());
      for (Object o : resourceTypesJsonArray) {
        if (o instanceof JsonObject subscriptionResourceTypeJson) {
          resourceTypes.add(subscriptionResourceTypeJson.mapTo(SubscriptionResourceType.class));
        } else {
          resourceTypes.add(new SubscriptionResourceType(ResourceType.fromValue(o.toString())));
        }
      }
    }
  }

  public static Subscription fromBson(JsonObject jsonObject) {
    return new Subscription(jsonObject);
  }

  public JsonObject toBson() {
    JsonObject jsonObject = toJson();
    jsonObject.remove(ID);
    jsonObject.put(BSON_ID, id);
    jsonObject.put(RESOURCE_TYPES, resourceTypes.stream().map(subscriptionResourceType -> subscriptionResourceType.getResource().toString()).collect(Collectors.toList()));
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

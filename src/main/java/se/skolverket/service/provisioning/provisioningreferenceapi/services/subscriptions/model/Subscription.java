package se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.ResourceType;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.ZoneOffset.UTC;
import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.Constants.*;

@Slf4j
@DataObject
@Getter
@Setter
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

  @JsonProperty(EXPIRES)
  @JsonFormat(timezone = "UTC")
  private ZonedDateTime expires;

  public Subscription(JsonObject jsonObject) {
    id = jsonObject.getString(ID, jsonObject.getString(BSON_ID));
    name = jsonObject.getString(NAME);
    target = jsonObject.getString(TARGET);
    try {
      expires = jsonObject.getString(EXPIRES) != null ? LocalDateTime.parse(jsonObject.getString(EXPIRES), DateTimeFormatter.ISO_DATE_TIME).atZone(UTC) : ZonedDateTime.now(UTC);
    } catch (Exception e) {
      ServiceException serviceException = new ServiceException(400, "Error parsing expires date for subscription");
      serviceException.initCause(e);
      log.error("Error parsing expires date for subscription", e);
      throw serviceException;
    }

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
    jsonObject.put(EXPIRES, jsonObject.getJsonObject(EXPIRES).getString("$date"));
    return new Subscription(jsonObject);
  }

  public JsonObject toBson() {
    JsonObject jsonObject = toJson();
    jsonObject.remove(ID);
    jsonObject.put(BSON_ID, id);
    jsonObject.put(RESOURCE_TYPES, resourceTypes.stream().map(subscriptionResourceType -> subscriptionResourceType.getResource().toString()).collect(Collectors.toList()));
    jsonObject.put(EXPIRES, new JsonObject().put("$date", expires.toInstant().toString()));
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

  public Subscription(String id, String name, String target, List<SubscriptionResourceType> resourceTypes, ZonedDateTime expires) {
    this.id = id;
    this.name = name;
    this.target = target;
    this.resourceTypes = resourceTypes;
    this.expires = expires;
  }
}

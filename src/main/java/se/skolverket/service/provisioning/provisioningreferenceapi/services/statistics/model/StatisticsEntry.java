package se.skolverket.service.provisioning.provisioningreferenceapi.services.statistics.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.Objects;

import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.Constants.DB_DATE;

@Slf4j
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@DataObject
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatisticsEntry {
  @JsonProperty("description")
  private String description;
  @JsonProperty("resourceType")
  private String resourceType;
  @JsonProperty("newCount")
  private Integer newCount;
  @JsonProperty("updatedCount")
  private Integer updatedCount;
  @JsonProperty("deletedCount")
  private Integer deletedCount;
  @JsonProperty("resourceUrl")
  private String resourceUrl;
  @JsonProperty("timeOfOccurance")
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private ZonedDateTime timeOfOccurance;


  public StatisticsEntry(JsonObject jsonObject) {
    this.description = jsonObject.getString("description");
    this.resourceType = jsonObject.getString("resourceType");
    this.newCount = jsonObject.getInteger("newCount");
    this.updatedCount = jsonObject.getInteger("updatedCount");
    this.deletedCount = jsonObject.getInteger("deletedCount");
    this.resourceUrl = jsonObject.getString("resourceUrl");
    this.timeOfOccurance = jsonObject.getString("timeOfOccurance") != null ? ZonedDateTime.parse(jsonObject.getString("timeOfOccurance")) : ZonedDateTime.now();
  }

  public static StatisticsEntry fromBson(JsonObject bsonObject) {
    try {
      bsonObject.put("timeOfOccurance", bsonObject.containsKey("timeOfOccurance") ? bsonObject.getJsonObject("timeOfOccurance").getString("$date") : null);
    } catch (NullPointerException e) {
      bsonObject.remove("timeOfOccurance");
      log.debug("timeOfOccurance of null in statistics entry. {}", bsonObject.encode());
    }
    return new StatisticsEntry(bsonObject);
  }

  public JsonObject toBson() {
    JsonObject jsonObject = this.toJson();
    if (Objects.nonNull(timeOfOccurance)) {
      jsonObject.put("timeOfOccurance", new JsonObject().put(DB_DATE, timeOfOccurance.toString()));
    }
    return jsonObject;
  }

  public JsonObject toJson() {
    return JsonObject.mapFrom(this);
  }
}

package se.skolverket.service.provisioning.provisioningreferenceapi.services.logging.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
public class LogEntry {
  @JsonProperty("message")
  private String message;
  @JsonProperty("messageType")
  private String messageType;
  @JsonProperty("resourceType")
  private String resourceType;
  @JsonProperty("resourceId")
  private String resourceId;
  @JsonProperty("resourceUrl")
  private String resourceUrl;
  @JsonProperty("severityLevel")
  private SeverityLevel severityLevel;
  @JsonProperty("timeOfOccurance")
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private ZonedDateTime timeOfOccurance;

  public LogEntry(JsonObject jsonObject) {
    LogEntry logEntry = jsonObject.mapTo(LogEntry.class);
    this.message = logEntry.getMessage();
    this.messageType = logEntry.getMessageType();
    this.resourceType = logEntry.getResourceType();
    this.resourceId = logEntry.getResourceId();
    this.resourceUrl = logEntry.getResourceUrl();
    this.severityLevel = logEntry.getSeverityLevel();
    try {
      this.timeOfOccurance = jsonObject.getString("timeOfOccurance") != null ? ZonedDateTime.parse(jsonObject.getString("timeOfOccurance")) : ZonedDateTime.now();
    } catch (Exception e) {
      this.timeOfOccurance = ZonedDateTime.now();
    }
  }

  public static LogEntry fromBson(JsonObject bsonObject) {
    try {
      bsonObject.put("timeOfOccurance", bsonObject.containsKey("timeOfOccurance") ? bsonObject.getJsonObject("timeOfOccurance").getString("$date") : null);
    } catch (NullPointerException e) {
      bsonObject.remove("timeOfOccurance");
      log.debug("timeOfOccurance of null in statistics entry. {}", bsonObject.encode());
    }
    return new LogEntry(bsonObject);
  }

  public JsonObject toBson() {
    JsonObject jsonObject = this.toJson();
    if (Objects.nonNull(timeOfOccurance)) {
      jsonObject.put("timeOfOccurance", new JsonObject().put(DB_DATE, timeOfOccurance.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)));
    }
    return jsonObject;
  }


  public JsonObject toJson() {
    return JsonObject.mapFrom(this);
  }
}

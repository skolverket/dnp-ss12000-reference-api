package se.skolverket.service.provisioning.provisioningreferenceapi.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.util.Objects;

import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.Constants.DB_DATE;

@Slf4j
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
public abstract class AbstractLogging {
  @JsonProperty("timeOfOccurance")
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private OffsetDateTime timeOfOccurance;
  @JsonProperty("resourceUrl")
  private String resourceUrl;
  @JsonProperty("resourceType")
  private String resourceType;

  protected AbstractLogging(JsonObject jsonObject) {
    this.resourceUrl = jsonObject.getString("resourceUrl");
    this.resourceType = jsonObject.getString("resourceType");

    try {
      this.timeOfOccurance = jsonObject.getString("timeOfOccurance") != null ? OffsetDateTime.parse(jsonObject.getString("timeOfOccurance")) : OffsetDateTime.now();
    } catch (Exception e) {
      log.warn("Error parsing timeOfOccurance. ", e);
      this.timeOfOccurance = OffsetDateTime.now();
    }
  }

  public AbstractLogging() {
  }

  protected static JsonObject parseBson(JsonObject bsonObject) {
    try {
      bsonObject.put("timeOfOccurance", bsonObject.containsKey("timeOfOccurance") ? bsonObject.getJsonObject("timeOfOccurance").getString("$date") : null);
    } catch (ClassCastException e) {
      bsonObject.put("timeOfOccurance", bsonObject.getString("timeOfOccurance"));
    } catch (NullPointerException e) {
      bsonObject.remove("timeOfOccurance");
      log.debug("timeOfOccurance of null. {}", bsonObject.encode());
    }
    return bsonObject;
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

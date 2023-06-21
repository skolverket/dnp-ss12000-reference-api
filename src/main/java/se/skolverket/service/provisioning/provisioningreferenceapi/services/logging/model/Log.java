package se.skolverket.service.provisioning.provisioningreferenceapi.services.logging.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@DataObject
@JsonIgnoreProperties(ignoreUnknown = true)
public class Log {
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

  public Log(JsonObject jsonObject) {
    Log log = jsonObject.mapTo(Log.class);
    this.message = log.getMessage();
    this.messageType = log.getMessageType();
    this.resourceType = log.getResourceType();
    this.resourceId = log.getResourceId();
    this.resourceUrl = log.getResourceUrl();
    this.severityLevel = log.getSeverityLevel();
  }

  public JsonObject toJson() {
    return JsonObject.mapFrom(this);
  }
}

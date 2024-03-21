package se.skolverket.service.provisioning.provisioningreferenceapi.services.logging.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.*;
import lombok.experimental.SuperBuilder;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.AbstractLogging;

@SuperBuilder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@DataObject
@JsonIgnoreProperties(ignoreUnknown = true)
public class Log extends AbstractLogging {
  @JsonProperty("message")
  private String message;
  @JsonProperty("messageType")
  private String messageType;
  @JsonProperty("resourceId")
  private String resourceId;
  @JsonProperty("severityLevel")
  private SeverityLevel severityLevel;

  public Log(JsonObject jsonObject) {
    super(jsonObject);
    Log log = jsonObject.mapTo(Log.class);
    this.message = log.getMessage();
    this.messageType = log.getMessageType();
    this.resourceId = log.getResourceId();
    this.severityLevel = log.getSeverityLevel();
  }

  public static Log fromBson(JsonObject bson) {
    return new Log(AbstractLogging.parseBson(bson));
  }
}

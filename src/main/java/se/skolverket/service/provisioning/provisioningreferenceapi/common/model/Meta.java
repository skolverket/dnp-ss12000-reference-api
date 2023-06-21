package se.skolverket.service.provisioning.provisioningreferenceapi.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import lombok.*;

import java.time.Instant;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Meta {

  @JsonProperty("created")
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private Instant created;

  @JsonProperty("modified")
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private Instant modified;

  public JsonObject toJson() {
    return JsonObject.mapFrom(this);
  }
}


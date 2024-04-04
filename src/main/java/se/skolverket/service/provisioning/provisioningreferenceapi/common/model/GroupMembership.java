package se.skolverket.service.provisioning.provisioningreferenceapi.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GroupMembership implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @JsonProperty("person")
  private PersonReference person;

  public JsonObject toJson() {
    return JsonObject.mapFrom(this);
  }

}

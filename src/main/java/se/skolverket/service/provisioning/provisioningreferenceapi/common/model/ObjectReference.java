package se.skolverket.service.provisioning.provisioningreferenceapi.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ObjectReference {
  @JsonProperty("id")
  private String id;
  @JsonProperty("displayName")
  private String displayName;

  public JsonObject toJson() {
    return JsonObject.mapFrom(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ObjectReference that = (ObjectReference) o;

    if (!id.equals(that.id)) return false;
    return displayName != null ? displayName.equals(that.displayName) : that.displayName == null;
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}

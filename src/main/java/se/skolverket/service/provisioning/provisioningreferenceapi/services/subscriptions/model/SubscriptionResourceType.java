package se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.ResourceType;


@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResourceType {
  @JsonProperty("resource")
  private ResourceType resource;

  public JsonObject toJson() {
    return JsonObject.mapFrom(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SubscriptionResourceType that = (SubscriptionResourceType) o;

    return resource == that.resource;
  }

  @Override
  public int hashCode() {
    return resource != null ? resource.hashCode() : 0;
  }

  @Override
  public String toString() {
    return toJson().encode();
  }
}

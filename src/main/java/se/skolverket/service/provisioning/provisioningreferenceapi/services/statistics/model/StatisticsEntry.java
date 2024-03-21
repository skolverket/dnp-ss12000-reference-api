package se.skolverket.service.provisioning.provisioningreferenceapi.services.statistics.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.AbstractLogging;

@Slf4j
@SuperBuilder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@DataObject
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatisticsEntry extends AbstractLogging {
  @JsonProperty("description")
  private String description;
  @JsonProperty("newCount")
  private Integer newCount;
  @JsonProperty("updatedCount")
  private Integer updatedCount;
  @JsonProperty("deletedCount")
  private Integer deletedCount;


  public StatisticsEntry(JsonObject jsonObject) {
    super(jsonObject);
    this.description = jsonObject.getString("description");
    this.newCount = jsonObject.getInteger("newCount");
    this.updatedCount = jsonObject.getInteger("updatedCount");
    this.deletedCount = jsonObject.getInteger("deletedCount");
  }

  public static StatisticsEntry fromBson(JsonObject bson) {
    return new StatisticsEntry(AbstractLogging.parseBson(bson));
  }



}

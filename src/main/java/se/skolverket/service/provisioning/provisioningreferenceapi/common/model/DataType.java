package se.skolverket.service.provisioning.provisioningreferenceapi.common.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.BsonConverterHelper;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class DataType {

  @JsonProperty("id")
  @JsonAlias("_id")
  protected String id;

  @JsonProperty("meta")
  protected Meta meta;

  public DataType(JsonObject jsonObject) {
    this.id = jsonObject.getString("id", jsonObject.getString("_id", null));
    setMeta(this, jsonObject);
  }

  public DataType() {
  }

  public JsonObject toJson() {
    return JsonObject.mapFrom(this);
  }

  public static <T> List<T> fromJsonArray(JsonArray jsonArray, Class<T> clazz) {
    List<T> objs = new LinkedList<>();
    for (int i = 0; i < jsonArray.size(); i++) {
      objs.add(jsonArray.getJsonObject(i).mapTo(clazz));
    }
    return objs;
  }

  public JsonObject toBson() {
    JsonObject jsonObject = JsonObject.mapFrom(this);
    jsonObject.put("_id", this.getId());
    jsonObject.remove("id");

    return jsonObject;
  }

  protected static void setMeta(DataType dataType, JsonObject bson) {

    if (bson.getJsonObject("meta") != null) {
      JsonObject metaJson = bson.getJsonObject("meta");
      String dateCreated = metaJson.getString("created") != null ? metaJson.getString("created") : null;
      String dateModified = metaJson.getString("modified") != null ? metaJson.getString("modified") : null;

      dataType.meta = new Meta();
      if(dateModified != null) {
        dataType.meta.setModified(Instant.parse(dateModified));
      }
      if(dateCreated != null) {
        dataType.meta.setCreated(Instant.parse(dateCreated));
      }
    } else {
      dataType.meta = null;
    }
  }

  protected static DataType fromBson(DataType dataType, JsonObject bson) {
    BsonConverterHelper.convertMetaToJson(bson);
    setMeta(dataType, bson);
    dataType.id = bson.getString("_id");

    return dataType;
  }
  public static JsonObject fromBsonJson(JsonObject bson) {
    BsonConverterHelper.convertMetaToJson(bson);
    bson.put("id", bson.remove("_id"));
    return bson;
  }

}

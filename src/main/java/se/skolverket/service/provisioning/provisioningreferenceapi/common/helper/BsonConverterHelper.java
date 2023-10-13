package se.skolverket.service.provisioning.provisioningreferenceapi.common.helper;

import io.vertx.core.json.JsonObject;

import java.time.Instant;

@SuppressWarnings("DeprecatedIsStillUsed")
public class BsonConverterHelper {

  private BsonConverterHelper() {
  }

  public static JsonObject convertMetaToJson(JsonObject bson) {
    JsonObject bsonMeta = bson.getJsonObject("meta");
    if (bsonMeta != null) {
      bson.remove("meta");

      JsonObject meta = new JsonObject();
      String dateCreated = bsonMeta.containsKey("created") ? bsonMeta.getJsonObject("created").getString("$date") : null;
      String dateModified = bsonMeta.containsKey("modified") ? bsonMeta.getJsonObject("modified").getString("$date") : null;

      meta.put("created", dateCreated).put("modified", dateModified);
      bson.put("meta", meta);
    }

    return bson;
  }

  /**
   * @param bson BSON formatted JsonObject
   * @return JsonObject with StartDate and EndDate formatted according to JsonObject default.
   */
  @Deprecated
  public static JsonObject convertStarEndDateToJson(JsonObject bson) {
    String startDateDate = bson.getJsonObject("startDate") != null ? bson.getJsonObject("startDate").getString("$date") : null;
    String endDateDate = bson.getJsonObject("endDate") != null ? bson.getJsonObject("endDate").getString("$date") : null;

    if (startDateDate != null) {
      bson.put("startDate", startDateDate.substring(0, startDateDate.lastIndexOf("T")));
    }
    if (endDateDate != null) {
      bson.put("endDate", endDateDate.substring(0, endDateDate.lastIndexOf("T")));
    }

    return bson;
  }


  @Deprecated
  public static JsonObject convertStarEndDateInArrayToJson(JsonObject jsonObject, String arrayKey) {
    if (jsonObject.getJsonArray(arrayKey) != null) {
      jsonObject.getJsonArray(arrayKey).forEach(bson -> {
        if (bson != null) {
          convertStarEndDateToJson((JsonObject) bson);
        }
      });
    }
    return jsonObject;
  }

  public static String timestampToObjectId(String timestamp) {
    Instant instant = Instant.parse(timestamp);
    long millis = instant.toEpochMilli();
    String hexMillis = Long.toHexString(millis / 1000L);

    return hexMillis.concat("0000000000000000");
  }
}

package se.skolverket.service.provisioning.provisioningreferenceapi.common.helper;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.BsonConverterHelper.timestampToObjectId;
import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.Constants.*;

public abstract class AbstractMetaDatabaseService {
  protected JsonObject buildQuery(JsonObject queryOptions) {
    String dateAfter = queryOptions.getJsonObject(PT_REQUEST).getString(QP_AFTER);
    String dateBefore = queryOptions.getJsonObject(PT_REQUEST).getString(QP_BEFORE);

    //If queryOptions contains 2 dates, we must wrap the dateQueries in an 'AND' array.
    if (dateAfter != null && dateBefore != null) {
      return new JsonObject().put(DB_AND, new JsonArray()
        .add(createDateAfterQuery(dateAfter))
        .add(createDateBeforeQuery(dateBefore)));
    } else {
      if (dateAfter != null) {
        return createDateAfterQuery(dateAfter);
      }
      if (dateBefore != null) {
        return createDateBeforeQuery(dateBefore);
      }
      return new JsonObject();
    }
  }

  protected JsonObject createDateBeforeQuery(String dateBefore) {
    return new JsonObject().put(BSON_ID, new JsonObject()
      .put(DB_LESS_THAN, new JsonObject().put(DB_OBJECT_ID, timestampToObjectId(dateBefore))));
  }

  protected JsonObject createDateAfterQuery(String dateAfter) {
    return new JsonObject().put(BSON_ID, new JsonObject()
      .put(DB_GREATER_THAN, new JsonObject().put(DB_OBJECT_ID, timestampToObjectId(dateAfter))));
  }
}

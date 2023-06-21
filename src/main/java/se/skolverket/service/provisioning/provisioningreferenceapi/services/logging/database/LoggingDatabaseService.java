package se.skolverket.service.provisioning.provisioningreferenceapi.services.logging.database;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.logging.model.Log;

import java.util.List;

public interface LoggingDatabaseService {
  Future<String> insertLog(Log log);
  Future<List<Log>> findLogs(JsonObject queryOptions);
}

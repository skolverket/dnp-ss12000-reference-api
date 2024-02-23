package se.skolverket.service.provisioning.provisioningreferenceapi.services.logging.database;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.logging.model.LogEntry;

import java.util.List;

public interface LoggingDatabaseService {
  Future<String> insertLog(LogEntry logEntry);
  Future<List<LogEntry>> findLogs(JsonObject queryOptions);
}

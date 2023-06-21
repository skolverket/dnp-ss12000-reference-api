package se.skolverket.service.provisioning.provisioningreferenceapi.services.logging.impl;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.logging.LoggingService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.logging.database.LoggingDatabaseService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.logging.model.Log;

import java.util.List;

public class LoggingServiceImpl implements LoggingService {

  private final LoggingDatabaseService loggingDatabaseService;

  public LoggingServiceImpl(LoggingDatabaseService loggingDatabaseService) {
    this.loggingDatabaseService = loggingDatabaseService;
  }

  @Override
  public Future<String> createLog(Log log) {
    return loggingDatabaseService.insertLog(log);
  }

  @Override
  public Future<List<Log>> getLogs(JsonObject queryOptions) {
    return loggingDatabaseService.findLogs(queryOptions);
  }
}

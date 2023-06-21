package se.skolverket.service.provisioning.provisioningreferenceapi.services.logging;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.ProxyIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.logging.database.LoggingDatabaseService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.logging.impl.LoggingServiceImpl;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.logging.model.Log;

import java.util.List;

@ProxyGen
@VertxGen
public interface LoggingService {
  String ADDRESS = "logging-service";

  @ProxyIgnore
  @GenIgnore
  static LoggingService create(LoggingDatabaseService loggingDatabaseService) {
    return new LoggingServiceImpl(loggingDatabaseService);
  }

  @ProxyIgnore
  @GenIgnore
  static LoggingService createProxy(Vertx vertx) {
    return new LoggingServiceVertxEBProxy(vertx, ADDRESS);
  }

  Future<String> createLog(Log log);

  Future<List<Log>> getLogs(JsonObject queryOptions);
}

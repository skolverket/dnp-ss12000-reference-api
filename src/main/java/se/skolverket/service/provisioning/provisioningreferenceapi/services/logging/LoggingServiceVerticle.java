package se.skolverket.service.provisioning.provisioningreferenceapi.services.logging;

import io.vertx.core.Promise;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.json.schema.Validator;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.serviceproxy.ServiceBinder;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.AbstractHttpServiceVerticle;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.ValidationHandlerFactory;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.logging.database.impl.LoggingDatabaseServiceImpl;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.logging.handler.LoggingHandler;

import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.DatabaseServiceHelper.parseMongoConfig;

@Slf4j
public class LoggingServiceVerticle extends AbstractHttpServiceVerticle {
  public static final String LOGGING_SERVICE_NAME = "log";
  private ServiceDiscovery serviceDiscovery;
  private static final JsonArray ALLOWED_METHODS = new JsonArray().add(HttpMethod.POST.toString());

  @Override
  public void start(Promise<Void> startPromise) {
    super.start(startPromise);
    log.info("Starting {}-service.", getServiceName());
    serviceDiscovery = ServiceDiscovery.create(vertx);

    String logSchemaUri = String.format("%s/schemas/log", schemaBaseUri);
    log.info("Resolving validator for schema: '{}'", logSchemaUri);
    Validator validator = schemaRepository.validator(logSchemaUri);

    // MongoDB Connection

    MongoClient mongoClient = MongoClient.createShared(vertx, parseMongoConfig(config())
        //Auto generated ids will be wrapped with ObjectId() - needed for filtering on the timestamp embedded in ObjectIds.
        .put("useObjectId", true),
      "logging_service_mongo_client");

    //Create an instance of LoggingService
    LoggingService _loggingService = LoggingService.create(new LoggingDatabaseServiceImpl(mongoClient));

    ServiceBinder binder = new ServiceBinder(vertx);
    //Register Handler
    MessageConsumer<JsonObject> consumer = binder
      .setAddress(LoggingService.ADDRESS)
      .register(LoggingService.class, _loggingService);

    //Proxy Service
    LoggingService loggingService = LoggingService.createProxy(vertx);

    Router router = Router.router(vertx);
    setRoutes(router, loggingService, validator);
    createHttpServer(router).onComplete(startPromise);
  }

  private void setRoutes(Router router, LoggingService loggingService, Validator validator) {
    router.route().handler(LoggerHandler.create());
    router.route().handler(BodyHandler.create());
    router.route().handler(ValidationHandlerFactory.create(validator));
    router.post("/").handler(LoggingHandler.postLog(loggingService));
    router.get("/").handler(LoggingHandler.getLogs(loggingService));
    router.route().failureHandler(defaultErrorHandler());
  }

  @Override
  public String getServiceName() {
    return LOGGING_SERVICE_NAME;
  }

  @Override
  protected ServiceDiscovery getServiceDiscovery() {
    return serviceDiscovery;
  }

  @Override
  protected String getHost() {
    return "localhost";
  }

  @Override
  protected Boolean isDataIngestEnabled() {
    return true;
  }

  @Override
  protected Boolean isExposed() {
    return true;
  }

  @Override
  protected JsonArray getAllowedMethods() {
    return ALLOWED_METHODS;
  }
}

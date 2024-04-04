package se.skolverket.service.provisioning.provisioningreferenceapi.services.statistics;

import io.vertx.core.Promise;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.json.schema.JsonSchemaOptions;
import io.vertx.json.schema.OutputFormat;
import io.vertx.json.schema.Validator;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.serviceproxy.ServiceBinder;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.AbstractHttpServiceVerticle;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.ValidationHandlerFactory;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.statistics.database.impl.StatisticsDatabaseServiceImpl;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.statistics.handler.StatisticsHandler;

import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.DatabaseServiceHelper.parseMongoConfig;

@Slf4j
public class StatisticsServiceVerticle extends AbstractHttpServiceVerticle {
  public static final String SERVICE_NAME = "statistics";
  private static final JsonArray ALLOWED_METHODS = new JsonArray().add(HttpMethod.POST.toString());
  private ServiceDiscovery serviceDiscovery;

  @Override
  public void start(Promise<Void> startPromise) {
    super.start(startPromise);
    log.info("Starting {}-service.", getServiceName());

    serviceDiscovery = ServiceDiscovery.create(vertx);

    String statisticsSchemaUri = String.format("%s/schemas/statistics", schemaBaseUri);
    log.info("Resolving validator for schema: '{}'", statisticsSchemaUri);
    Validator validator = schemaRepository.validator(statisticsSchemaUri, new JsonSchemaOptions().setOutputFormat(OutputFormat.Basic).setBaseUri("example.com"));

    // MongoDB Connection

    MongoClient mongoClient = MongoClient.createShared(vertx, parseMongoConfig(config())
      .put("useObjectId", true),
      "statistics_service_mongo_client");
    //Create an instance of LoggingService
    StatisticsService _statisticsService = StatisticsService.create(new StatisticsDatabaseServiceImpl(mongoClient));

    ServiceBinder binder = new ServiceBinder(vertx);
    //Register Handler
    MessageConsumer<JsonObject> consumer = binder
      .setAddress(StatisticsService.ADDRESS)
      .register(StatisticsService.class, _statisticsService);

    //Proxy Service
    StatisticsService statisticsService = StatisticsService.createProxy(vertx);

    Router router = Router.router(vertx);
    setRoutes(router, statisticsService, validator);
    createHttpServer(router).onComplete(startPromise);
  }

  private void setRoutes(Router router, StatisticsService service, Validator validator) {
    router.route().handler(LoggerHandler.create());
    router.route().handler(BodyHandler.create());
    router.route().blockingHandler(ValidationHandlerFactory.create(validator));
    router.post("/").handler(StatisticsHandler.postStatisticsEntry(service));
    router.get("/").handler(StatisticsHandler.getStatistics(service));
    router.route().failureHandler(defaultErrorHandler());
  }

  @Override
  public String getServiceName() {
    return SERVICE_NAME;
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

package se.skolverket.service.provisioning.provisioningreferenceapi.services.duties;

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
import se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.DeletedEntitiesService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.database.impl.DutiesDatabaseServiceImpl;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.handler.DutiesHandler;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.PersonsService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.SubscriptionsService;

import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.DatabaseServiceHelper.parseMongoConfig;

@Slf4j
public class DutiesServiceVerticle extends AbstractHttpServiceVerticle {

  public static final String DUTIES_SERVICE_NAME = "duties";

  private static final JsonArray ALLOWED_METHODS = new JsonArray()
    .add(HttpMethod.GET.toString());

  private ServiceDiscovery serviceDiscovery;

  @Override
  public void start(Promise<Void> startPromise) {
    super.start(startPromise);
    log.info("Starting {}-service", getServiceName());

    serviceDiscovery = ServiceDiscovery.create(vertx);

    String dutySchemaUri = String.format("%s/schemas/duties", schemaBaseUri);
    log.info("Resolving validator for schema: '{}'", dutySchemaUri);
    Validator validator = schemaRepository.validator(dutySchemaUri);

    // MongoDB Connection
    MongoClient mongoClient = MongoClient.createShared(vertx, parseMongoConfig(config()));
    DeletedEntitiesService deletedEntitiesService = DeletedEntitiesService.createProxy(vertx);
    PersonsService personsService = PersonsService.createProxy(vertx);
    SubscriptionsService subscriptionsService = SubscriptionsService.createProxy(vertx);
    DutiesService _dutiesService = DutiesService.create(new DutiesDatabaseServiceImpl(mongoClient),
      deletedEntitiesService, personsService, subscriptionsService);

    //Register the handler
    MessageConsumer<JsonObject> consumer = bindService(_dutiesService);

    // Proxy service
    DutiesService dutiesService = DutiesService.createProxy(vertx);

    Router router = Router.router(vertx);
    setRoutes(router, dutiesService, validator);

    createHttpServer(router)
      .onComplete(startPromise);
  }

  private MessageConsumer<JsonObject> bindService(DutiesService dutiesService) {
    return new ServiceBinder(vertx)
      .setAddress(getServiceName() + "-service")
      .register(DutiesService.class, dutiesService);
  }

  private void setRoutes(Router router, DutiesService dutiesService, Validator validator) {
    router.route().handler(LoggerHandler.create());
    router.route().handler(BodyHandler.create());
    router.route().handler(ValidationHandlerFactory.create(validator));
    router.post("/").handler(DutiesHandler.postDuties(dutiesService));
    router.delete("/").handler(DutiesHandler.deleteDuties(dutiesService));
    router.put("/").handler(DutiesHandler.putDuties(dutiesService));
    router.get("/").handler(DutiesHandler.getDuties(dutiesService));
    router.route().failureHandler(defaultErrorHandler());
  }

  @Override
  public String getServiceName() {
    return DUTIES_SERVICE_NAME;
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

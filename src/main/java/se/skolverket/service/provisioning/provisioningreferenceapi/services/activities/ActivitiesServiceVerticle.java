package se.skolverket.service.provisioning.provisioningreferenceapi.services.activities;

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
import se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.database.ActivitiesDatabaseService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.database.impl.ActivitiesDatabaseServiceImpl;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.handler.ActivitiesHandler;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.impl.ActivitiesServiceImpl;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.DeletedEntitiesService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.DutiesService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.GroupsService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.SubscriptionsService;

import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.DatabaseServiceHelper.parseMongoConfig;

@Slf4j
public class ActivitiesServiceVerticle extends AbstractHttpServiceVerticle {
  public static final String SERVICE_NAME = "activities";

  private ServiceDiscovery serviceDiscovery;

  private static final JsonArray ALLOWED_METHODS = new JsonArray()
    .add(HttpMethod.GET.toString());

  @Override
  public void start(Promise<Void> startPromise) {
    super.start(startPromise);
    log.info("Starting {}-service", getServiceName());

    serviceDiscovery = ServiceDiscovery.create(vertx);

    String activitiesSchemaUri = String.format("%s/schemas/activities", schemaBaseUri);
    log.info("Resolving validator for schema: '{}'", activitiesSchemaUri);
    Validator validator = schemaRepository.validator(activitiesSchemaUri);

    // MongoDB Connection
    MongoClient mongoClient = MongoClient.createShared(vertx, parseMongoConfig(config()));
    ActivitiesDatabaseService activitiesDatabaseService = new ActivitiesDatabaseServiceImpl(mongoClient);

    // Create an instance of ActivitiesService.
    DeletedEntitiesService deletedEntitiesService = DeletedEntitiesService.createProxy(vertx);
    GroupsService groupService = GroupsService.createProxy(vertx);
    DutiesService dutiesService = DutiesService.createProxy(vertx);
    SubscriptionsService subscriptionsService = SubscriptionsService.createProxy(vertx);
    ActivitiesServiceImpl streamingActivitiesService = new ActivitiesServiceImpl(activitiesDatabaseService, deletedEntitiesService,
      groupService, dutiesService, subscriptionsService);

    serviceDiscovery = ServiceDiscovery.create(vertx);
    ServiceBinder binder = new ServiceBinder(vertx);

    // Register the handler
    MessageConsumer<JsonObject> consumer = binder
      .setAddress(ActivitiesService.ADDRESS)
      .register(ActivitiesService.class, streamingActivitiesService);

    Router router = Router.router(vertx);
    setRoutes(router, streamingActivitiesService, validator);

    createHttpServer(router)
      .onComplete(startPromise);
  }

  private void setRoutes(Router router, ActivitiesServiceImpl activitiesService, Validator validator) {
    router.route().handler(LoggerHandler.create());
    router.route().handler(BodyHandler.create());
    router.route().handler(ValidationHandlerFactory.create(validator));

    router.post("/").handler(ActivitiesHandler.postActivities(activitiesService));
    router.put("/").handler(ActivitiesHandler.putActivities(activitiesService));
    router.delete("/").handler(ActivitiesHandler.deleteActivities(activitiesService));
    router.get("/").handler(ActivitiesHandler.getActivities(activitiesService));
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

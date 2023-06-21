package se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities;

import io.vertx.core.Promise;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.serviceproxy.ServiceBinder;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.AbstractHttpServiceVerticle;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.database.impl.DeletedEntitiesDatabaseServiceImpl;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.handler.DeletedEntitiesHandler;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.SubscriptionsService;

import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.DatabaseServiceHelper.parseMongoConfig;

@Slf4j
public class DeletedEntitiesVerticle extends AbstractHttpServiceVerticle {

  public static final String DELETED_ENTITIES_SERVICE_NAME = "deletedEntities";
  private static final JsonArray ALLOWED_METHODS = new JsonArray()
    .add(HttpMethod.GET.toString());
  private ServiceDiscovery serviceDiscovery;

  @Override
  public void start(Promise<Void> startPromise) {
    super.start(startPromise);
    log.info("Starting {}-service", getServiceName());

    serviceDiscovery = ServiceDiscovery.create(vertx);

    // MongoDB Connection

    MongoClient mongoClient = MongoClient.createShared(vertx, parseMongoConfig(config())
      //Auto generated ids will be wrapped with ObjectId() - used for filtering on the timestamp embedded in ObjectIds.
      .put("useObjectId", true),
      "deleted_entities_mongo_client");

    SubscriptionsService subscriptionsService = SubscriptionsService.createProxy(vertx);
    // Register the handler
    MessageConsumer<JsonObject> consumer = bindService(DeletedEntitiesService
      .create(new DeletedEntitiesDatabaseServiceImpl(mongoClient), subscriptionsService));

    DeletedEntitiesService deletedEntitiesService = DeletedEntitiesService.createProxy(vertx);
    Router router = Router.router(vertx);
    setRoutes(router, deletedEntitiesService);

    createHttpServer(router)
      .onComplete(startPromise);
  }

  private MessageConsumer<JsonObject> bindService(DeletedEntitiesService deletedEntitiesService) {
    return new ServiceBinder(vertx)
      .setAddress(getServiceName() + "-service")
      .register(DeletedEntitiesService.class, deletedEntitiesService);
  }

  private void setRoutes(Router router, DeletedEntitiesService deletedEntitiesService) {
    router.route().handler(LoggerHandler.create());
    router.route().handler(BodyHandler.create());
    router.get("/").handler(DeletedEntitiesHandler.getDeletedEntities(deletedEntitiesService));
    router.route().failureHandler(defaultErrorHandler());
  }

  @Override
  public String getServiceName() {
    return DELETED_ENTITIES_SERVICE_NAME;
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
    return false;
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

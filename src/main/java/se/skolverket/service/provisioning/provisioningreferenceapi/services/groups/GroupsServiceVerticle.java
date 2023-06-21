package se.skolverket.service.provisioning.provisioningreferenceapi.services.groups;

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
import se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.database.impl.GroupsDatabaseServiceImpl;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.handler.GroupsHandler;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.PersonsService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.SubscriptionsService;

import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.DatabaseServiceHelper.parseMongoConfig;

@Slf4j
public class GroupsServiceVerticle extends AbstractHttpServiceVerticle {

  public static final String GROUP_SERVICE_NAME = "groups";

  private ServiceDiscovery serviceDiscovery;

  private static final JsonArray ALLOWED_METHODS = new JsonArray()
    .add(HttpMethod.GET.toString());

  @Override
  public void start(Promise<Void> startPromise) {
    super.start(startPromise);
    log.info("Starting {}-service", getServiceName());
    serviceDiscovery = ServiceDiscovery.create(vertx);

    // Set Validator
    String groupSchemaUri = String.format("%s/schemas/groups", schemaBaseUri);
    log.info("Resolving validator for schema: '{}'", groupSchemaUri);
    Validator validator = schemaRepository.validator(groupSchemaUri);

    // MongoDB Connection
    MongoClient mongoClient = MongoClient.createShared(vertx, parseMongoConfig(config()));
    DeletedEntitiesService deletedEntitiesService = DeletedEntitiesService.createProxy(vertx);
    PersonsService personsService = PersonsService.createProxy(vertx);
    SubscriptionsService subscriptionsService = SubscriptionsService.createProxy(vertx);
    GroupsService _groupsService = GroupsService.create(new GroupsDatabaseServiceImpl(mongoClient),
      deletedEntitiesService, personsService, subscriptionsService);

    //Register the handler
    MessageConsumer<JsonObject> consumer = bindService(_groupsService);

    // Proxy service
    GroupsService groupsService = GroupsService.createProxy(vertx);

    Router router = Router.router(vertx);
    setRoutes(router, groupsService, validator);

    createHttpServer(router)
      .onComplete(startPromise);
  }

  private MessageConsumer<JsonObject> bindService(GroupsService groupsService) {
    return new ServiceBinder(vertx)
      .setAddress(getServiceName() + "-service")
      .register(GroupsService.class, groupsService);
  }

  private void setRoutes(Router router, GroupsService groupsService, Validator validator) {
    router.route().handler(LoggerHandler.create());
    router.route().handler(BodyHandler.create());
    router.route().handler(ValidationHandlerFactory.create(validator));
    router.get("/").handler(GroupsHandler.getGroups(groupsService));
    router.post("/").handler(GroupsHandler.postGroups(groupsService));
    router.put("/").handler(GroupsHandler.putGroups(groupsService));
    router.delete("/").handler(GroupsHandler.deleteGroups(groupsService));
    router.route().failureHandler(defaultErrorHandler());
  }

  @Override
  public String getServiceName() {
    return GROUP_SERVICE_NAME;
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

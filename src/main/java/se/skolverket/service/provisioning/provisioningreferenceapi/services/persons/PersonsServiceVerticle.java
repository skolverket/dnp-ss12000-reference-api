package se.skolverket.service.provisioning.provisioningreferenceapi.services.persons;

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
import se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.database.PersonsDatabaseService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.database.impl.PersonsDatabaseServiceImpl;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.handler.PersonsHandler;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.impl.PersonsServiceImpl;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.SubscriptionsService;

import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.DatabaseServiceHelper.parseMongoConfig;

@Slf4j
public class PersonsServiceVerticle extends AbstractHttpServiceVerticle {

  public static final String SERVICE_NAME = "persons";
  private static final JsonArray ALLOWED_METHODS = new JsonArray()
    .add(HttpMethod.GET.toString());
  private ServiceDiscovery serviceDiscovery;

  @Override
  public void start(Promise<Void> startPromise) {
    super.start(startPromise);
    log.info("Starting {}-service.", getServiceName());

    serviceDiscovery = ServiceDiscovery.create(vertx);

    String personSchemaUri = String.format("%s/schemas/persons", schemaBaseUri);
    log.info("Resolving validator for schema: '{}'", personSchemaUri);
    Validator validator = schemaRepository.validator(personSchemaUri);

    // MongoDB Connection
    MongoClient mongoClient = MongoClient.createShared(vertx, parseMongoConfig(config()));
    PersonsDatabaseService personsDatabaseService = new PersonsDatabaseServiceImpl(mongoClient);

    // Create an instance of PersonsService.
    DeletedEntitiesService deletedEntitiesService = DeletedEntitiesService.createProxy(vertx);
    SubscriptionsService subscriptionsService = SubscriptionsService.createProxy(vertx);
    PersonsServiceImpl streamingPersonsService = new PersonsServiceImpl(personsDatabaseService, deletedEntitiesService, subscriptionsService);

    ServiceBinder binder = new ServiceBinder(vertx);

    // Register the handler
    MessageConsumer<JsonObject> consumer = binder
      .setAddress(PersonsService.ADDRESS)
      .register(PersonsService.class, streamingPersonsService);

    Router router = Router.router(vertx);
    setRoutes(router, streamingPersonsService, validator);

    createHttpServer(router)
      .onComplete(startPromise);
  }

  private void setRoutes(Router router, PersonsServiceImpl personsService, Validator validator) {
    router.route().handler(LoggerHandler.create());
    router.route().handler(BodyHandler.create());
    router.route().handler(ValidationHandlerFactory.create(validator));
    router.get("/").handler(PersonsHandler.getPersons(personsService));
    router.post("/").handler(PersonsHandler.postPersons(personsService));
    router.put("/").handler(PersonsHandler.putPersons(personsService));
    router.delete("/").handler(PersonsHandler.deletePersons(personsService));
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

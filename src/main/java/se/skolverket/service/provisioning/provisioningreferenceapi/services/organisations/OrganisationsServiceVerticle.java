package se.skolverket.service.provisioning.provisioningreferenceapi.services.organisations;

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
import se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.DeletedEntitiesService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.organisations.database.impl.OrganisationsDatabaseServiceImpl;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.organisations.handler.OrganisationsHandler;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.organisations.impl.OrganisationsServiceImpl;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.SubscriptionsService;

import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.DatabaseServiceHelper.parseMongoConfig;

@Slf4j
public class OrganisationsServiceVerticle extends AbstractHttpServiceVerticle {
  public static final String SERVICE_NAME = "organisations";
  private static final JsonArray ALLOWED_METHODS = new JsonArray()
    .add(HttpMethod.GET.toString());
  private ServiceDiscovery serviceDiscovery;

  @Override
  public void start(Promise<Void> startPromise) {
    super.start(startPromise);
    log.info("Starting {}-service.", getServiceName());

    serviceDiscovery = ServiceDiscovery.create(vertx);

    String organisationsSchemaUri = String.format("%s/schemas/organisations", schemaBaseUri);
    log.info("Resolving validator for schema: '{}'", organisationsSchemaUri);
    Validator validator = schemaRepository.validator(organisationsSchemaUri, new JsonSchemaOptions().setOutputFormat(OutputFormat.Basic).setBaseUri("example.com"));

    // MongoDB Connection
    MongoClient mongoClient = MongoClient.createShared(vertx, parseMongoConfig(config()));
    OrganisationsDatabaseServiceImpl organisationsDatabaseService = new OrganisationsDatabaseServiceImpl(mongoClient);

    // Create an instance of OrganisationsService.
    DeletedEntitiesService deletedEntitiesService = DeletedEntitiesService.createProxy(vertx);
    SubscriptionsService subscriptionsService = SubscriptionsService.createProxy(vertx);
    OrganisationsServiceImpl streamingOrganisationsService = new OrganisationsServiceImpl(organisationsDatabaseService, deletedEntitiesService, subscriptionsService);

    ServiceBinder binder = new ServiceBinder(vertx);

    // Register the handler
    MessageConsumer<JsonObject> consumer = binder
      .setAddress(OrganisationsService.ADDRESS)
      .register(OrganisationsService.class, streamingOrganisationsService);

    Router router = Router.router(vertx);
    setRoutes(router, streamingOrganisationsService, validator);

    createHttpServer(router)
      .onComplete(startPromise);
  }

  private void setRoutes(Router router, OrganisationsServiceImpl organisationsService, Validator validator) {
    router.route().handler(LoggerHandler.create());
    router.route().handler(BodyHandler.create());
    router.route().blockingHandler(ValidationHandlerFactory.create(validator));
    router.get("/").handler(OrganisationsHandler.get(organisationsService));
    router.post("/").handler(OrganisationsHandler.post(organisationsService));
    router.put("/").handler(OrganisationsHandler.put(organisationsService));
    router.delete("/").handler(OrganisationsHandler.delete(organisationsService));
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

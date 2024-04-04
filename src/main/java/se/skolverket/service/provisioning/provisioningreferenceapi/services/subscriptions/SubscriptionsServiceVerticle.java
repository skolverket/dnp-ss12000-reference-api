package se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions;

import io.vertx.core.Promise;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
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
import se.skolverket.service.provisioning.provisioningreferenceapi.common.WebClientOptionsWithProxyOptions;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.database.SubscriptionsDatabaseService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.database.impl.SubscriptionsDatabaseServiceImpl;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.handler.SubscriptionsHandler;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.impl.CircuitBreakerFactoryImpl;
import se.skolverket.service.provisioning.provisioningreferenceapi.token.GuardianOfTheTokenService;

import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.Constants.PP_SUBSCRIPTION_ID;
import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.DatabaseServiceHelper.parseMongoConfig;


@Slf4j
public class SubscriptionsServiceVerticle extends AbstractHttpServiceVerticle {
  public static final String SERVICE_NAME = "subscriptions";
  // How many attempts after attempt #1 are permitted.
  private static final int NOTIFICATION_RETRIES = 3;
  // The time a notification is allowed to take before timing out.
  private static final long NOTIFICATION_TIMEOUT_MS = 5000L;
  // Duration after the first attempt during which retries are permitted.
  private static final long NOTIFICATION_RETRY_TIMEOUT_MS = 300000L;
  // The time it takes for the circuit breaker to go from an open state to a
  // half-open state in case of reaching the maximum allowed failures (probably
  // due to faulty subscriptions or connectivity problems). NB. a half-open
  // circuit breaker does not allow retries.
  private static final long CIRCUIT_BREAKER_RESET_TIMEOUT_MS = 1L;
  private static final JsonArray ALLOWED_METHODS = new JsonArray()
    .add(HttpMethod.POST.toString())
    .add(HttpMethod.DELETE.toString());
  private ServiceDiscovery serviceDiscovery;

  @Override
  public void start(Promise<Void> startPromise) {
    super.start(startPromise);
    log.info("Starting {}-service.", getServiceName());

    serviceDiscovery = ServiceDiscovery.create(vertx);

    String subscriptionSchemaUri = String.format("%s/schemas/subscription", schemaBaseUri);
    log.info("Resolving validator for schema: '{}'", subscriptionSchemaUri);
    Validator validator = schemaRepository.validator(subscriptionSchemaUri, new JsonSchemaOptions().setOutputFormat(OutputFormat.Basic).setBaseUri("example.com"));

    // MongoDB Connection
    MongoClient mongoClient = MongoClient.createShared(vertx, parseMongoConfig(config()));
    SubscriptionsDatabaseService subscriptionsDatabaseService = new SubscriptionsDatabaseServiceImpl(mongoClient);

    CircuitBreakerFactory circuitBreakerFactory = new CircuitBreakerFactoryImpl();

    WebClient webClient = WebClient.create(vertx, WebClientOptionsWithProxyOptions.create(config()));
    SubscriptionsService _subscriptionsService = SubscriptionsService.create(
      subscriptionsDatabaseService, vertx, circuitBreakerFactory, webClient, vertx.sharedData(), GuardianOfTheTokenService.createProxy(vertx)
    );

    ServiceBinder binder = new ServiceBinder(vertx);

    // Register the handler
    MessageConsumer<JsonObject> consumer = binder
      .setAddress(SubscriptionsService.ADDRESS)
      .register(SubscriptionsService.class, _subscriptionsService);

    // Proxy service
    SubscriptionsService subscriptionsService = SubscriptionsService.createProxy(vertx);

    Router router = Router.router(vertx);
    setRoutes(router, subscriptionsService, validator);

    createHttpServer(router)
      .onComplete(startPromise);
  }

  private void setRoutes(Router router, SubscriptionsService subscriptionsService, Validator validator) {
    router.route().handler(LoggerHandler.create());
    router.route().handler(BodyHandler.create());
    router.route().blockingHandler(ValidationHandlerFactory.create(validator));
    router.post("/").handler(SubscriptionsHandler.postSubscriptions(subscriptionsService));
    router.delete(String.format("/:%s", PP_SUBSCRIPTION_ID)).handler(SubscriptionsHandler.deleteSubscriptions(subscriptionsService));
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

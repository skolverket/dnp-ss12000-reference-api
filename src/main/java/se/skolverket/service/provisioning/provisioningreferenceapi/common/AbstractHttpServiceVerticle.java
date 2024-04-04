package se.skolverket.service.provisioning.provisioningreferenceapi.common;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.json.schema.Draft;
import io.vertx.json.schema.JsonSchema;
import io.vertx.json.schema.JsonSchemaOptions;
import io.vertx.json.schema.SchemaRepository;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.serviceproxy.ServiceException;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.exception.ValidationFailedException;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.FileSystemException;
import java.util.List;

import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.Constants.CONFIG_KEY_VALIDATION_BASE_URI;
import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.Constants.CONFIG_KEY_VALIDATION_BASE_URI_DEFAULT;
import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.RequestHelper.response500Error;
import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.RequestHelper.responseError;

@Slf4j
public abstract class AbstractHttpServiceVerticle extends AbstractVerticle {

  protected static String schemaBaseUri;
  protected Integer port;
  protected SchemaRepository schemaRepository;

  public abstract String getServiceName();

  protected abstract ServiceDiscovery getServiceDiscovery();

  protected abstract String getHost();

  protected abstract Boolean isDataIngestEnabled();

  protected abstract Boolean isExposed();

  public void start(Promise<Void> startPromise) {
    schemaBaseUri = config()
      .getString(CONFIG_KEY_VALIDATION_BASE_URI, CONFIG_KEY_VALIDATION_BASE_URI_DEFAULT);
    populateSchemaRepository();
  }

  protected Future<Void> createHttpServer(Router router) {
    Promise<Void> createHttpServerPromise = Promise.promise();

    port = config().getInteger("port");
    if (port == null) {
      try {
        port = findRandomOpenPortOnAllLocalInterfaces();
      } catch (IOException e) {
        log.error("Error getting port.", e);
        createHttpServerPromise.fail(e);
        return createHttpServerPromise.future();
      }
    }

    vertx.createHttpServer()
      .requestHandler(router)
      .listen(port, res -> {
        if (res.succeeded()) {
          port = res.result().actualPort();
          publishHttpEndpoint(getServiceDiscovery(), port, "/", isDataIngestEnabled(), isExposed())
            .onFailure(cause -> {
              log.error("Error publishing service.", cause);
              createHttpServerPromise.fail(cause);
            })
            .onSuccess(v -> {
              log.info("Service {} started on port: {}", getServiceName(), port);
              createHttpServerPromise.complete();
            });
        } else {
          log.error("Error starting Http Server for {} ", getServiceName(), res.cause());
          createHttpServerPromise.fail(res.cause());
        }
      });

    return createHttpServerPromise.future();
  }

  private Future<Record> publishHttpEndpoint(ServiceDiscovery discovery, Integer port, String root, Boolean dataIngest, Boolean expose) {
    Promise<Record> publishPromise = Promise.promise();
    Record record1 = HttpEndpoint.createRecord(
      getServiceName(), // The service name
      getHost(), // The host
      port, // the port
      root, // the root of the service
      new JsonObject()
        .put("ingest", dataIngest)
        .put("expose", expose)
        .put("allowedMethods", getAllowedMethods())
    );

    discovery.publish(record1, ar -> publishPromise.complete());
    return publishPromise.future();
  }

  protected Integer findRandomOpenPortOnAllLocalInterfaces() throws IOException {
    try (
      ServerSocket socket = new ServerSocket(0)
    ) {
      return socket.getLocalPort();
    }
  }

  protected Handler<RoutingContext> defaultErrorHandler() {
    return routingContext -> {
      log.info("Temp using log, defaultErrorHandler routingContext: {}", routingContext);

      if (routingContext.failed()) {
        log.info("Temp using log, defaultErrorHandler routingContext failure: {}", routingContext.failure().getMessage());

        if (routingContext.failure() instanceof ServiceException) {
          ServiceException serviceException = (ServiceException) routingContext.failure();
          responseError(routingContext, (serviceException.failureCode() > 299 && serviceException.failureCode() < 501) ? serviceException.failureCode() : 500, new JsonObject().put("message", serviceException.getMessage()).put("error", serviceException.getDebugInfo() == null ? new JsonObject() : serviceException.getDebugInfo()));
        } else if (routingContext.failure() instanceof ValidationFailedException) {
          ValidationFailedException validationFailedException = (ValidationFailedException) routingContext.failure();
          responseError(routingContext, 400, new JsonObject().put("errors", validationFailedException.getValidationErrors()));
        } else {
          log.error("Error in " + getServiceName() + ".", routingContext.failure());
          response500Error(routingContext);
        }
      } else {
        routingContext.next();
      }
    };
  }

  private void populateSchemaRepository() {
    schemaRepository = SchemaRepository.create(
      new JsonSchemaOptions().setBaseUri(schemaBaseUri).setDraft(Draft.DRAFT202012)
    );

    try {
      for (String schemaPath : getSchemaPaths()) {
        schemaRepository.dereference(
          JsonSchema.of(
            new JsonObject(
              vertx.fileSystem().readFileBlocking(schemaPath)
            )
          )
        );
      }
    } catch (FileSystemException e) {
      e.printStackTrace();
      log.error("failed to fetch schema paths");
    }
  }

  private List<String> getSchemaPaths() throws FileSystemException {
    try {
      return vertx.fileSystem().readDirBlocking("schema");
    } catch (NullPointerException e) {
      e.printStackTrace();
      throw new FileSystemException("failed to read 'schema' directory");
    }
  }


  protected JsonArray getAllowedMethods() {
    return new JsonArray();
  }
}

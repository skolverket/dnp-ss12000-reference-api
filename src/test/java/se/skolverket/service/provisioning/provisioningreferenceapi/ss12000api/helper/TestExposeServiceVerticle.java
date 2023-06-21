package se.skolverket.service.provisioning.provisioningreferenceapi.ss12000api.helper;

import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.servicediscovery.ServiceDiscovery;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.AbstractHttpServiceVerticle;

import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.RequestHelper.response200Json;

/**
 * Test helper that publishes a service ('test-service') to the cluster with meta-tag 'ingest' set to true and 'expose' to false.
 */
public class TestExposeServiceVerticle extends AbstractHttpServiceVerticle {

  private ServiceDiscovery serviceDiscovery;

  @Override
  public void start(Promise<Void> startPromise) {
    serviceDiscovery = ServiceDiscovery.create(vertx);
    Router router = Router.router(vertx);
    router.route("/*").handler(ctx -> response200Json(ctx,
      new JsonObject().put("message", "Hello World").put("path", ctx.request().uri())));
    createHttpServer(router)
      .onComplete(startPromise);
  }

  @Override
  public String getServiceName() {
    return "test-service";
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
    return new JsonArray().add(HttpMethod.GET.toString());
  }
}


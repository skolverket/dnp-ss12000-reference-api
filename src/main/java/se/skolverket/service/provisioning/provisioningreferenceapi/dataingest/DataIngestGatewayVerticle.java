package se.skolverket.service.provisioning.provisioningreferenceapi.dataingest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.predicate.ResponsePredicateResult;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceReference;
import lombok.extern.slf4j.Slf4j;

/**
 * Delegates requests to services registered to the cluster with the meta-tag 'ingest' set to true.
 */
@Slf4j
public class DataIngestGatewayVerticle extends AbstractVerticle {

  private ServiceDiscovery serviceDiscovery;

  @Override
  public void start(Promise<Void> startPromise) {
    log.info("Starting DataIngestGatewayVerticle.");

    serviceDiscovery = ServiceDiscovery.create(vertx);

    Router router = Router.router(vertx);

    router.route().handler(LoggerHandler.create());
    router.route().handler(BodyHandler.create().setHandleFileUploads(false));
    router.get("/openapi/*").handler(StaticHandler.create("openapi/ingest").setIndexPage("index.html"));
    router.routeWithRegex("\\/(?<service>[^\\/]+)(\\/*.*)").handler(this::dispatchRequest);

    vertx.createHttpServer(new HttpServerOptions().setCompressionSupported(true)).requestHandler(router).listen(8889, http -> {
      if (http.succeeded()) {
        log.info("DataIngestGatewayVerticle started on port {}", http.result().actualPort());
        startPromise.complete();
      } else {
        startPromise.fail(http.cause());
      }
    });
  }

  private void dispatchRequest(RoutingContext routingContext) {
    String serviceName = routingContext.pathParam("service");
    String servicePath = routingContext.request().uri().substring(serviceName.length() + 1); // Length of "/[service name]"
    log.info("New service request to '{}', dispatching request to URI '{}'.", serviceName, servicePath);
    serviceDiscovery.getRecord(new JsonObject().put("name", serviceName).put("ingest", true),
      ar -> {
        if (ar.succeeded() && ar.result() != null) {
          // Retrieve the service reference
          ServiceReference reference = serviceDiscovery.getReference(ar.result());
          // Retrieve the service object
          WebClient webClient = reference.getAs(WebClient.class);

          // Prepend '/' in case of query parameters to avoid a router exception
          final String finalSvcPath = servicePath.length() > 0 && servicePath.charAt(0) == '?' ? "/" + servicePath : servicePath;

          // Prepare request to underlying service and pipe response from service to client reply.
          HttpServerResponse clientResponse = routingContext.response()
            .putHeader("Content-Type", "application/json")
            .setChunked(true);

          HttpRequest<Void> request = webClient.request(routingContext.request().method(), finalSvcPath)
            .as(BodyCodec.pipe(clientResponse, false))
            .putHeaders(routingContext.request().headers())
            .expect(voidHttpResponse -> {
              clientResponse.setStatusCode(voidHttpResponse.statusCode());
              return ResponsePredicateResult.success();
            });

          Future<HttpResponse<Void>> serviceReply;
          if (routingContext.body() != null) {
            serviceReply = request.sendBuffer(routingContext.body().buffer());
          } else {
            serviceReply = request.send();
          }
          serviceReply.onSuccess(serviceResponse -> {
            clientResponse.end();
          }).onFailure(e -> {
            log.error("Error dispatching request to service. ", e);
            routingContext.fail(500, e);
          }).eventually(v -> {
            try {
              webClient.close();
              reference.release();
            } catch (Exception e) {
              // NOP, This is ok.
            }
            return Future.succeededFuture(v);
          });
        } else {
          log.error("Error getting service with name {}.", serviceName, ar.cause());
          routingContext.fail(404);
        }
      });
  }

}

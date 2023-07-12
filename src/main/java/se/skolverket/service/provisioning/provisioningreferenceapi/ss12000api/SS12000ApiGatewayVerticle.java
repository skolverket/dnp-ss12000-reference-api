package se.skolverket.service.provisioning.provisioningreferenceapi.ss12000api;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceReference;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.RequestHelper;

import java.util.List;
import java.util.stream.Collectors;

import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.Constants.CONFIG_SS12000_AUTH_IGNORE_JWT_WEBHOOKS;
import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.Constants.CONFIG_SS12000_AUTH_JWKS_URI;


/**
 * Delegates requests to services registered to the cluster with the meta-tag 'expose' set to true.
 */
@Slf4j
public class SS12000ApiGatewayVerticle extends AbstractVerticle {

  private ServiceDiscovery serviceDiscovery;

  @Override
  public void start(Promise<Void> startPromise) {
    log.info("Starting SS12000ApiGatewayVerticle.");

    serviceDiscovery = ServiceDiscovery.create(vertx);

    initAuth()
      .onSuccess(routingContextHandler -> {
        Router router = Router.router(vertx);

        router.route().handler(LoggerHandler.create());
        router.route().handler(routingContextHandler);
        router.route().handler(BodyHandler.create().setHandleFileUploads(false));
        router.get("/openapi/*").handler(StaticHandler.create("openapi/expose").setIndexPage("index.html"));
        router.routeWithRegex("\\/(?<service>[^\\/]+)(\\/*.*)").handler(this::dispatchRequest);


        vertx.createHttpServer().requestHandler(router).listen(8888, http -> {
          if (http.succeeded()) {
            log.info("SS12000ApiGatewayVerticle started on port {}", http.result().actualPort());
            startPromise.complete();
          } else {
            startPromise.fail(http.cause());
          }
        });
      })
      .onFailure(startPromise::fail);


  }

  private void dispatchRequest(RoutingContext routingContext) {
    String serviceName = routingContext.pathParam("service");
    String servicePath = routingContext.request().uri().substring(serviceName.length() + 1); // Length of "/[service name]"

    log.info("New service request to '{}', dispatching request to URI '{}'.", serviceName, servicePath);
    serviceDiscovery.getRecord(new JsonObject().put("name", serviceName).put("expose", true),
      ar -> {
        if (ar.succeeded() && ar.result() != null) {
          if (ar.result().getMetadata().getJsonArray("allowedMethods").contains(routingContext.request().method().toString())) {
            if (!RequestHelper.queryParamsValid(routingContext.request().params())) {
              log.error("Invalid query parameters, it is not allowed to pair "+
                "'pageToken' with any other parameter than 'limit'.");
              routingContext.fail(400);
              return;
            }

            // Retrieve the service reference
            ServiceReference reference = serviceDiscovery.getReference(ar.result());
            // Retrieve the service object
            WebClient webClient = reference.getAs(WebClient.class);
            // Prepend '/' in case of query parameters to avoid a router exception
            final String finalSvcPath =
              servicePath.length() > 0 && servicePath.charAt(0) == '?' ?
                "/" + servicePath : servicePath;
            HttpRequest<Buffer> request = webClient.request(routingContext.request().method(), finalSvcPath);
            request.putHeaders(routingContext.request().headers());
            Future<HttpResponse<Buffer>> serviceReply;
            if (routingContext.body() != null) {
              serviceReply = request.sendBuffer(routingContext.body().buffer());
            } else {
              serviceReply = request.send();
            }
            serviceReply.onSuccess(httpResponse -> {
              HttpServerResponse response = routingContext.response()
                .setStatusCode(httpResponse.statusCode());
              httpResponse.headers().forEach(header -> response.putHeader(header.getKey(), header.getValue()));
              if (httpResponse.body() != null) {
                response.end(httpResponse.body());
              } else {
                response.end();
              }
            }).onFailure(e -> {
              log.error("Error dispatching request to service. ", e);
              routingContext.fail(500);
            }).eventually(v -> {
              try {
                reference.release();
              } catch (Exception e) {
                // NOP, This is ok.
              }
              return Future.succeededFuture(v);
            });
          } else {
            routingContext.fail(404);
          }

        } else {
          log.error("Error getting service with name {}.", serviceName, ar.cause());
          routingContext.fail(404);
        }
      });
  }

  private Future<Handler<RoutingContext>> initAuth() {
    Promise<Handler<RoutingContext>> setupAuthPromise = Promise.promise();
    try {
      if (isAuthEnabled()) {
        getJWKsAndConfigureAuthProvider()
          .onFailure(setupAuthPromise::fail)
          .onSuccess(jwtAuth -> setupAuthPromise.complete(JWTAuthHandler.create(jwtAuth)));
      } else {
        log.warn("JWT Auth is disabled for SS12000 API Gateway. See documentation and `{}`", CONFIG_SS12000_AUTH_IGNORE_JWT_WEBHOOKS);
        setupAuthPromise.complete(RoutingContext::next);
      }
    } catch (Exception e) {
      log.error("Error initAuth.", e);
      setupAuthPromise.fail(e);
    }
    return setupAuthPromise.future();
  }
  private Future<JWTAuth> getJWKsAndConfigureAuthProvider() {
    String uri = config().getString(CONFIG_SS12000_AUTH_JWKS_URI);
    WebClient webClient = WebClient.create(vertx);
    return webClient.getAbs(uri)
      .as(BodyCodec.jsonObject())
      .send()
      .compose(jwksJsonObjectResponse -> {
        if (jwksJsonObjectResponse.statusCode() == 200) {
          log.info("JWKs from {} : {}", uri, jwksJsonObjectResponse.body().encode());
          List<JsonObject> jwks = jwksJsonObjectResponse.body().getJsonArray("keys").stream()
            .map(o -> (JsonObject) o)
            .collect(Collectors.toList());
          JWTAuthOptions config = new JWTAuthOptions()
            .setJwks(jwks);
          JWTAuth provider = JWTAuth.create(vertx, config);
          return Future.succeededFuture(provider);
        } else {
          log.error("Error getting JWKs from source. URI: {}, Status {}, Body: {}", uri, jwksJsonObjectResponse.statusCode(), jwksJsonObjectResponse.bodyAsString());
          return Future.failedFuture(new Exception("Error getting JWKs."));
        }

      })
      .onFailure(throwable -> log.error("Error getting JWKs from source. URI: {}", uri, throwable));
  }

  private boolean isAuthEnabled() throws RuntimeException {
    try {
      if (config().containsKey(CONFIG_SS12000_AUTH_IGNORE_JWT_WEBHOOKS)) {
        return !Boolean.parseBoolean(config().getString(CONFIG_SS12000_AUTH_IGNORE_JWT_WEBHOOKS));
      } else {
        return true;
      }
    } catch (Exception e) {
      return true;
    }
  }

}

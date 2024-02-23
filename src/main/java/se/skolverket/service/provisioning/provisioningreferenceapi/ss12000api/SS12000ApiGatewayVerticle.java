package se.skolverket.service.provisioning.provisioningreferenceapi.ss12000api;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.client.predicate.ResponsePredicateResult;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceReference;
import io.vertx.serviceproxy.ServiceException;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.WebClientOptionsWithProxyOptions;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.RequestHelper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.Constants.*;


/**
 * Delegates requests to services registered to the cluster with the meta-tag 'expose' set to true.
 */
@Slf4j
public class SS12000ApiGatewayVerticle extends AbstractVerticle {

  private ServiceDiscovery serviceDiscovery;

  private String allowedLocation = CONFIG_AUTH_JWT_CLAIM_LOCATION_DEFAULT;
  private String allowedOrganizationId;

  @Override
  public void start(Promise<Void> startPromise) {
    log.info("Starting SS12000ApiGatewayVerticle.");

    allowedLocation = config().getString(CONFIG_AUTH_JWT_CLAIM_LOCATION, CONFIG_AUTH_JWT_CLAIM_LOCATION_DEFAULT);
    allowedOrganizationId = config().getString(CONFIG_AUTH_JWT_CLAIM_ORGANIZATION_ID, null);
    log.info("Location for SS12000ApiGatewayVerticle: {}", allowedLocation);

    serviceDiscovery = ServiceDiscovery.create(vertx);

    initAuth()
      .onSuccess(routingContextHandler -> {
        log.info("Temp using log, initAuth routingContext success: {}", routingContextHandler);

        Router router = Router.router(vertx);

        router.route().handler(LoggerHandler.create());
        router.route().handler(routingContextHandler);
        router.route().handler(this::handleAuth);
        router.route().handler(BodyHandler.create().setHandleFileUploads(false));
        router.get("/openapi/*").handler(StaticHandler.create("openapi/expose").setIndexPage("index.html"));
        router.routeWithRegex("\\/(?<service>[^\\/]+)(\\/*.*)").handler(this::dispatchRequest);


        vertx.createHttpServer(new HttpServerOptions().setCompressionSupported(true)).requestHandler(router).listen(8888, http -> {
          if (http.succeeded()) {
            log.info("SS12000ApiGatewayVerticle started on port {}", http.result().actualPort());
            startPromise.complete();
          } else {
            startPromise.fail(http.cause());
          }
        });
      })
      .onFailure(cause -> {
        log.info("Temp using log, initAuth routingContext failure: {}", cause, cause);

        startPromise.fail(cause);
      });


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
              log.error("Invalid query parameters, it is not allowed to pair " +
                "'pageToken' with any other parameter than 'limit'.");
              routingContext.fail(400);
              return;
            }

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
            serviceReply.onSuccess(serviceResponse ->
              clientResponse.end()).onFailure(e -> {
              log.error("Error dispatching request to service. ", e);
              routingContext.fail(500, e);
            }).eventually(unused -> {
              try {
                webClient.close();
                reference.release();
              } catch (Exception e) {
                // NOP, This is ok.
              }
              return Future.succeededFuture();
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

  private void handleAuth(RoutingContext routingContext) {
    if (isAuthEnabled()) {
      if (allowedOrganizationId != null) {
        String claimOrganizationId = routingContext.user().principal().getString("organization_id");
        if (!allowedOrganizationId.equals(claimOrganizationId)) {
          routingContext.fail(401, new ServiceException(401, "JWT Claim organization_id does not contain configured allowed organization_id."));
          return;
        }
      }
      boolean requestedAccessContainsThisLocation = false;
      JsonArray requestedAccessClaim = routingContext.user().principal().getJsonArray("requested_access");
      Optional<JsonObject> optionalRequestedAccessSs12000Node = requestedAccessClaim.stream()
        .map(o -> (JsonObject) o)
        .filter(jsonObject -> jsonObject.getString("type", "").equals(JWT_REQUESTED_ACCESS_TYPE))
        .findFirst();
      if (optionalRequestedAccessSs12000Node.isPresent()) {
        requestedAccessContainsThisLocation = optionalRequestedAccessSs12000Node.get().getJsonArray("locations")
          .stream()
          .map(Object::toString)
          .anyMatch(s -> s.contains(allowedLocation));
      }

      if (requestedAccessContainsThisLocation) {
        routingContext.next();
      } else {
        routingContext.fail(403, new ServiceException(403, "JWT Claim requested_access.location does not contain this location."));
      }
    } else {
      routingContext.next();
    }
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

    WebClientOptions webClientProxyOptions = WebClientOptionsWithProxyOptions.create(config())
      .setSsl(true);

    WebClient webClient = WebClient.create(vertx, webClientProxyOptions);
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

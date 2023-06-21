package se.skolverket.service.provisioning.provisioningreferenceapi.helper;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.LoggerFormat;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.extension.ExtendWith;
import se.skolverket.service.provisioning.provisioningreferenceapi.ProvisioningReferenceApiAbstractTest;

@ExtendWith(VertxExtension.class)
public abstract class AbstractRestApiHelper extends ProvisioningReferenceApiAbstractTest {

  protected Future<Integer> mockServer(Vertx vertx, HttpMethod httpMethod, String routingPath, Handler<RoutingContext> handler, VertxTestContext testContext) {
    registerJavaTimeModule();

    Router router = Router.router(vertx);
    router.route().handler(LoggerHandler.create(true, LoggerFormat.TINY));
    router.route().handler(LoggerHandler.create(false, LoggerFormat.DEFAULT));
    if (httpMethod.equals(HttpMethod.POST) || httpMethod.equals(HttpMethod.PUT)) {
      router.route().handler(BodyHandler.create());
    }
    router.route(httpMethod, routingPath).handler(handler);
    return vertx.createHttpServer()
      .requestHandler(router)
      .listen(findRandomOpenPortOnAllLocalInterfaces())
      .onFailure(testContext::failNow)
      .compose(httpServer -> Future.succeededFuture(httpServer.actualPort()));
  }
}

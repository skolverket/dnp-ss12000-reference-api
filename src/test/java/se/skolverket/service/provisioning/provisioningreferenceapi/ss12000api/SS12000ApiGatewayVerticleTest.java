package se.skolverket.service.provisioning.provisioningreferenceapi.ss12000api;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import se.skolverket.service.provisioning.provisioningreferenceapi.ss12000api.helper.TestExposeServiceVerticle;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(VertxExtension.class)
class SS12000ApiGatewayVerticleTest {

  @BeforeEach
  @DisplayName("Deploy a verticle")
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(TestExposeServiceVerticle.class.getName())
      .compose(s -> vertx.deployVerticle(SS12000ApiGatewayVerticle.class.getName(), new DeploymentOptions().setConfig(new JsonObject().put("SS12000_AUTH_IGNORE_JWT_WEBHOOKS", "true"))))
      .onComplete(testContext.succeedingThenComplete());
  }

  @Test
  @DisplayName("HTTP GET /test-service")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void get(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    client.get(8888, "localhost", String.format("/%s", "test-service"))
      .send()
      .onComplete(
        testContext.succeeding(bufferHttpResponse ->
          testContext.verify(() -> {
            assertEquals(200, bufferHttpResponse.statusCode());
            assertEquals("Hello World", bufferHttpResponse.bodyAsJsonObject().getString("message"));
            testContext.completeNow();
          })
        )
      );
  }

  @Test
  @DisplayName("HTTP DELETE /test-service")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void delete404(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    client.delete(8888, "localhost", String.format("/%s", "test-service"))
      .send()
      .onComplete(
        testContext.succeeding(bufferHttpResponse ->
          testContext.verify(() -> {
            assertEquals(404, bufferHttpResponse.statusCode());
            testContext.completeNow();
          })
        )
      );
  }

  @Test
  @DisplayName("HTTP GET /test-service/sub-path")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void getSubPath(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    client.get(8888, "localhost", String.format("/%s/sub-path", "test-service"))
      .send()
      .onComplete(
        testContext.succeeding(bufferHttpResponse ->
          testContext.verify(() -> {
            assertEquals(200, bufferHttpResponse.statusCode());
            assertEquals("Hello World", bufferHttpResponse.bodyAsJsonObject().getString("message"));
            assertEquals("/sub-path", bufferHttpResponse.bodyAsJsonObject().getString("path"));
            testContext.completeNow();
          })
        )
      );
  }


}

package se.skolverket.service.provisioning.provisioningreferenceapi.dataingest;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import se.skolverket.service.provisioning.provisioningreferenceapi.dataingest.helper.TestIngestServiceVerticle;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.TimeUnit;


@ExtendWith(VertxExtension.class)
class DataIngestGatewayVerticleTest {

  @BeforeEach
  @DisplayName("Deploy a verticle")
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(TestIngestServiceVerticle.class.getName())
      .compose(s -> vertx.deployVerticle(DataIngestGatewayVerticle.class.getName()))
      .onComplete(testContext.succeedingThenComplete());
  }

  @Test
  @DisplayName("HTTP GET /test-service")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void get(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);
    client.get(8889, "localhost", String.format("/%s", "test-service"))
      .send()
      .onComplete(
        testContext.succeeding(bufferHttpResponse ->
          testContext.verify(() -> {
            assertEquals(200, bufferHttpResponse.statusCode());
            assertEquals("Hello World", bufferHttpResponse.bodyAsJsonObject().getString("message"));
            assertEquals("application/json", bufferHttpResponse.getHeader("Content-Type"));
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
    client.get(8889, "localhost", String.format("/%s/sub-path", "test-service"))
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

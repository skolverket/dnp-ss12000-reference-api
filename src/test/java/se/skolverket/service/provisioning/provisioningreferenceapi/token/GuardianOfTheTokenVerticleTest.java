package se.skolverket.service.provisioning.provisioningreferenceapi.token;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxTestContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.skolverket.service.provisioning.provisioningreferenceapi.ProvisioningReferenceApiAbstractTest;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
class GuardianOfTheTokenVerticleTest extends ProvisioningReferenceApiAbstractTest {

  private final JsonObject config = new JsonObject()
    .put("AUTH_URI", "https://nutid-auth-test.sunet.se/transaction")
    .put("AUTH_CLIENT_KEY", "https://login-test.skolverket.se")
    .put("AUTH_PKCS_PATH", "src/main/resources/pkcs/ss12k-ref.p12")
    .put("AUTH_PKCS_PASSWORD", "Bfv@U4bT5yzL3s7B")
    .put("AUTH_ALIAS", "ss12k-ref");

  @Test
  @DisplayName("Test GuardianOfTheTokenVerticle Deployment")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void testDeployment(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(GuardianOfTheTokenVerticle.class.getName(), new DeploymentOptions().setConfig(config))
      .onComplete(testContext.succeeding(s -> testContext.verify(() -> {
          Set<String> ids = vertx.deploymentIDs();
          assertNotNull(ids);
          testContext.completeNow();
        }))
      );
  }
}

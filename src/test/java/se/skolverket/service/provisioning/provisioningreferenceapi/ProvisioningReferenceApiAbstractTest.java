package se.skolverket.service.provisioning.provisioningreferenceapi;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.ServerSocket;

@ExtendWith(VertxExtension.class)
public abstract class ProvisioningReferenceApiAbstractTest {

  @BeforeAll
  @DisplayName("Configure system")
  public static void configure(Vertx vertx, VertxTestContext testContext) {
    registerJavaTimeModule();
    vertx.setTimer(100, id -> testContext.completeNow()); // For some reason on slower systems the tests fail without this.
  }

  protected static void registerJavaTimeModule() {
    MainVerticle.registerJavaTimeModule();
  }

  public static Integer findRandomOpenPortOnAllLocalInterfaces() {
    try (
      ServerSocket socket = new ServerSocket(0)
    ) {
      return socket.getLocalPort();
    } catch (Exception e) {
      e.printStackTrace();
      return 8989;
    }
  }
}

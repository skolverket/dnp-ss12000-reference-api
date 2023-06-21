package se.skolverket.service.provisioning.provisioningreferenceapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vertx.core.Vertx;
import io.vertx.core.json.jackson.DatabindCodec;
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
    testContext.completeNow();
  }

  protected static void registerJavaTimeModule() {
    ObjectMapper mapper = DatabindCodec.mapper();
    mapper.registerModule(new JavaTimeModule());
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

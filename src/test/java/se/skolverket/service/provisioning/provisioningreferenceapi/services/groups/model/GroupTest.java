package se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.model;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import se.skolverket.service.provisioning.provisioningreferenceapi.ProvisioningReferenceApiAbstractTest;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class GroupTest extends ProvisioningReferenceApiAbstractTest {

  @Test
  void fromJsonToBson(Vertx vertx, VertxTestContext testContext) {
    testContext.verify(() -> {
      Buffer buffer = vertx.fileSystem().readFileBlocking(Objects.requireNonNull(getClass().getClassLoader().getResource("sampledata/group_json.json")).getPath());
      JsonObject groupJson = new JsonObject(buffer);
      Group group = new Group(groupJson);

      JsonObject bson = group.toBson();
      assertEquals("Undervisning", bson.getString("groupType"));
      testContext.completeNow();
    });
  }
}

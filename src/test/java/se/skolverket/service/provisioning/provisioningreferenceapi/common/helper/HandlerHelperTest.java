package se.skolverket.service.provisioning.provisioningreferenceapi.common.helper;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.*;
import io.vertx.junit5.VertxTestContext;
import io.vertx.serviceproxy.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import se.skolverket.service.provisioning.provisioningreferenceapi.ProvisioningReferenceApiAbstractTest;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.model.Activity;

import java.util.Objects;

class HandlerHelperTest extends ProvisioningReferenceApiAbstractTest {

  private RequestBody requestBody;
  private RoutingContext routingContext;

  @BeforeEach
  void setup(Vertx vertx, VertxTestContext testContext) {
    requestBody = Mockito.mock(RequestBody.class);
    routingContext = Mockito.mock(RoutingContext.class);
    Mockito.when(routingContext.body()).thenReturn(requestBody);
    testContext.completeNow();
  }

  @Test
  void getBodyAndParseActivity(Vertx vertx, VertxTestContext testContext) {
    try {
      Buffer buffer = vertx.fileSystem().readFileBlocking(Objects.requireNonNull(getClass().getClassLoader().getResource("sampledata/activities_invalid.json")).getPath());
      JsonObject activityJson = new JsonObject(buffer);
      Mockito.when(requestBody.asJsonObject()).thenReturn(activityJson);
      HandlerHelper.getBodyAndParse(routingContext, Activity.class, "activities");
      testContext.failNow("Should have not gotten here. ");
    } catch (ServiceException e) {
      testContext.completeNow();
    }
  }

}

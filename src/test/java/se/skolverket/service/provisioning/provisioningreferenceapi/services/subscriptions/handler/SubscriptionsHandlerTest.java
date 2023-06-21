package se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.handler;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import se.skolverket.service.provisioning.provisioningreferenceapi.helper.AbstractRestApiHelper;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.SubscriptionsService;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.Constants.PP_SUBSCRIPTION_ID;
import static se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.helper.SubscriptionHelper.validSubscription;

@ExtendWith(VertxExtension.class)
class SubscriptionsHandlerTest extends AbstractRestApiHelper {

  private SubscriptionsService subscriptionsService;

  private static final String BASE_URI = "/subscriptions";

  @BeforeEach
  void setup(Vertx vertx, VertxTestContext vertxTestContext) {
    subscriptionsService = Mockito.mock(SubscriptionsService.class);
    vertxTestContext.completeNow();
  }

  @Test
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void postSubscription(Vertx vertx, VertxTestContext vertxTestContext) {
    Mockito.when(subscriptionsService.createSubscription(any())).thenReturn(Future.succeededFuture(validSubscription()));

    mockServer(vertx, HttpMethod.POST, BASE_URI, SubscriptionsHandler.postSubscriptions(subscriptionsService), vertxTestContext)
      .onComplete(vertxTestContext.succeeding(port -> {
        WebClient client = WebClient.create(vertx);
        client.post(port, "localhost", BASE_URI)
          .sendJsonObject(validSubscription().toJson())
          .onComplete(vertxTestContext.succeeding(bufferHttpResponse ->
            vertxTestContext.verify(() -> {
              assertEquals(201, bufferHttpResponse.statusCode());
              vertxTestContext.completeNow();
            })
          ));
      }));
  }

  @Test
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void deleteSubscription(Vertx vertx, VertxTestContext vertxTestContext) {
    Mockito.when(subscriptionsService.deleteSubscription(anyString())).thenReturn(Future.succeededFuture());

    mockServer(
      vertx, HttpMethod.DELETE, BASE_URI + String.format("/:%s", PP_SUBSCRIPTION_ID),
      SubscriptionsHandler.deleteSubscriptions(subscriptionsService), vertxTestContext
    ).onComplete(vertxTestContext.succeeding(port -> {
        WebClient client = WebClient.create(vertx);
        client.delete(port, "localhost", BASE_URI + "/" + UUID.randomUUID())
          .send()
          .onComplete(vertxTestContext.succeeding(bufferHttpResponse ->
            vertxTestContext.verify(() -> {
              assertEquals(204, bufferHttpResponse.statusCode());
              vertxTestContext.completeNow();
            })
          ));
      }));
  }
}

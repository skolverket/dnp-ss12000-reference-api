package se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.impl;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.ResourceType;
import se.skolverket.service.provisioning.provisioningreferenceapi.helper.GuardianOfTheTokenHelperMockService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.CircuitBreakerFactory;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.SubscriptionsService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.database.SubscriptionsDatabaseService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.model.Subscription;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;
import static se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.helper.SubscriptionHelper.validSubscription;

@Slf4j
@ExtendWith(VertxExtension.class)
class SubscriptionsServiceImplCircuitBreakerTest {

  private SubscriptionsDatabaseService subscriptionsDatabaseService;
  private SubscriptionsService subscriptionsService;
  private CircuitBreaker circuitBreaker;

  private WebClient webClient;

  @BeforeEach
  void setup(Vertx vertx, VertxTestContext vertxTestContext) {
    subscriptionsDatabaseService = mock(SubscriptionsDatabaseService.class);
    circuitBreaker = CircuitBreaker.create(
      "testing-circuit-breaker", vertx,
      new CircuitBreakerOptions()
        .setResetTimeout(1L)
        .setTimeout(100L)
        .setMaxRetries(3)
        .setMaxFailures(1)
    );
    circuitBreaker.retryPolicy(count -> 10L);
    CircuitBreakerFactory circuitBreakerFactory = mock(CircuitBreakerFactory.class);
    when(circuitBreakerFactory.getCircuitBreaker(any(), any())).thenReturn(circuitBreaker);
    webClient = mock(WebClient.class);
    subscriptionsService = new SubscriptionsServiceImpl(
      subscriptionsDatabaseService, vertx, circuitBreakerFactory, webClient, vertx.sharedData(), new GuardianOfTheTokenHelperMockService()
    );
    vertxTestContext.completeNow();
  }

  @Test
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void circuitBreakerRetriesOnCallFailure(Vertx vertx, VertxTestContext vertxTestContext) {
    Checkpoint checkpoint = vertxTestContext.checkpoint(3);
    Subscription subscription = validSubscription();
    HttpRequest<Buffer> requestMock = mock(HttpRequest.class);
    when(requestMock.sendJsonObject(any()))
      .thenReturn(Future.failedFuture(new Exception()));
    when(webClient.postAbs((String) argThat(a -> a.equals(subscription.getTarget()))))
      .thenReturn(requestMock);
    when(subscriptionsDatabaseService.getSubscriptionsOf(any()))
      .thenReturn(Future.succeededFuture(List.of(subscription)));

    circuitBreaker.openHandler(v -> {
      log.info("open handler called");
      verify(subscriptionsDatabaseService, times(1)).getSubscriptionsOf(argThat(a -> a.equals(ResourceType.DUTY)));
      verify(webClient, times(4)).postAbs((String) argThat(a -> a.equals(subscription.getTarget())));
      checkpoint.flag();
    });
    circuitBreaker.halfOpenHandler(v -> {
      log.info("half-open handler called");
      checkpoint.flag();
    });

    subscriptionsService.dataChanged(ResourceType.DUTY, false)
      .onFailure(v -> checkpoint.flag())
      .onSuccess(t -> vertxTestContext.failNow("dataChanged() call should have failed"));
  }

  @Test
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void circuitBreakerRetriesOnFailureStatusCode(Vertx vertx, VertxTestContext vertxTestContext) {
    Checkpoint checkpoint = vertxTestContext.checkpoint(3);
    Subscription subscription = validSubscription();
    HttpRequest<Buffer> requestMock = mock(HttpRequest.class);
    HttpResponse<Buffer> responseMock = mock(HttpResponse.class);
    when(responseMock.statusCode()).thenReturn(404);
    when(requestMock.sendJsonObject(any()))
      .thenReturn(Future.succeededFuture(responseMock));
    when(webClient.postAbs((String) argThat(a -> a.equals(subscription.getTarget()))))
      .thenReturn(requestMock);
    when(subscriptionsDatabaseService.getSubscriptionsOf(any()))
      .thenReturn(Future.succeededFuture(List.of(subscription)));

    circuitBreaker.openHandler(v -> {
      log.info("open handler called");
      verify(subscriptionsDatabaseService, times(1)).getSubscriptionsOf(argThat(a -> a.equals(ResourceType.DUTY)));
      verify(webClient, times(4)).postAbs((String) argThat(a -> a.equals(subscription.getTarget())));
      checkpoint.flag();
    });
    circuitBreaker.halfOpenHandler(v -> {
      log.info("half-open handler called");
      checkpoint.flag();
    });

    subscriptionsService.dataChanged(ResourceType.DUTY, false)
      .onFailure(v -> checkpoint.flag())
      .onSuccess(t -> vertxTestContext.failNow("dataChanged() call should have failed"));
  }
}

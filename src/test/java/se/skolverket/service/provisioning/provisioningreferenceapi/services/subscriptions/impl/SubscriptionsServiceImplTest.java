package se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.impl;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.serviceproxy.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.ResourceType;
import se.skolverket.service.provisioning.provisioningreferenceapi.helper.GuardianOfTheTokenHelperMockService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.CircuitBreakerFactory;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.SubscriptionsService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.database.SubscriptionsDatabaseService;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;
import static se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.helper.SubscriptionHelper.validSubscription;

@ExtendWith(VertxExtension.class)
class SubscriptionsServiceImplTest {

  private SubscriptionsDatabaseService subscriptionsDatabaseService;
  private SubscriptionsService subscriptionsService;
  private CircuitBreaker circuitBreaker;

  @BeforeEach
  void setup(Vertx vertx, VertxTestContext vertxTestContext) {
    subscriptionsDatabaseService = mock(SubscriptionsDatabaseService.class);
    circuitBreaker = mock(CircuitBreaker.class);
    CircuitBreakerFactory circuitBreakerFactory = mock(CircuitBreakerFactory.class);
    when(circuitBreakerFactory.getCircuitBreaker(any(), any())).thenReturn(circuitBreaker);
    WebClient webClient = mock(WebClient.class);
    subscriptionsService = new SubscriptionsServiceImpl(
      subscriptionsDatabaseService, vertx, circuitBreakerFactory, webClient, vertx.sharedData(), new GuardianOfTheTokenHelperMockService()
    );
    vertxTestContext.completeNow();
  }

  @Test
  void createSubscription(VertxTestContext vertxTestContext) {
    when(subscriptionsDatabaseService.insertSubscription(any()))
      .thenReturn(Future.succeededFuture(validSubscription()));

    subscriptionsService.createSubscription(validSubscription())
      .onSuccess(s -> {
        verify(subscriptionsDatabaseService, times(1))
          .insertSubscription(any());
        vertxTestContext.completeNow();
      })
      .onFailure(t -> vertxTestContext.failNow("subscription create should have succeeded"));
  }

  @Test
  void deleteSubscription(VertxTestContext vertxTestContext) {
    when(subscriptionsDatabaseService.deleteSubscription(anyString()))
      .thenReturn(Future.succeededFuture());

    subscriptionsService.deleteSubscription("some-id")
      .onSuccess(v -> {
        verify(subscriptionsDatabaseService, times(1))
          .deleteSubscription(anyString());
        vertxTestContext.completeNow();
      })
      .onFailure(t -> vertxTestContext.failNow("subscription delete should have succeeded"));
  }

  @Test
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void dataChangedModified(VertxTestContext vertxTestContext) {
    when(subscriptionsDatabaseService.getSubscriptionsOf(ResourceType.ACTIVITY))
      .thenReturn(Future.succeededFuture(List.of(validSubscription())));
    when(circuitBreaker.execute(any()))
      .thenReturn(Future.succeededFuture());

    subscriptionsService.dataChanged(ResourceType.ACTIVITY, false)
      .onSuccess(v -> {
        verify(subscriptionsDatabaseService).getSubscriptionsOf(
          argThat(rt -> rt.equals(ResourceType.ACTIVITY))
        );
        verify(subscriptionsDatabaseService, times(0))
          .deleteSubscription(anyString());
        verify(circuitBreaker, times(1)).execute(any());

        vertxTestContext.completeNow();
      })
      .onFailure(v -> vertxTestContext.failNow("dataChanged call should have succeeded"));
  }

  @Test
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void dataChangedModifiedMultipleSubscriptions(VertxTestContext vertxTestContext) {
    when(subscriptionsDatabaseService.getSubscriptionsOf(ResourceType.ACTIVITY))
      .thenReturn(Future.succeededFuture(List.of(validSubscription(), validSubscription("another-valid-subscription"))));
    when(subscriptionsDatabaseService.deleteSubscription(anyString()))
      .thenReturn(Future.succeededFuture());
    when(circuitBreaker.execute(any()))
      .thenReturn(Future.succeededFuture());

    subscriptionsService.dataChanged(ResourceType.ACTIVITY, false)
      .onSuccess(v -> {
        verify(subscriptionsDatabaseService).getSubscriptionsOf(
          argThat(rt -> rt.equals(ResourceType.ACTIVITY))
        );
        verify(circuitBreaker, times(2)).execute(any());

        vertxTestContext.completeNow();
      })
      .onFailure(t -> vertxTestContext.failNow("dataChanged call should have succeeded"));
  }

  @Test
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void dataChangedDeleted(VertxTestContext vertxTestContext) {
    when(subscriptionsDatabaseService.getSubscriptions())
      .thenReturn(Future.succeededFuture(List.of(validSubscription(), validSubscription("another-valid-subscription"))));
    when(circuitBreaker.execute(any()))
      .thenReturn(Future.succeededFuture());

    subscriptionsService.dataChanged(ResourceType.ACTIVITY, true)
      .onSuccess(v -> {
        verify(subscriptionsDatabaseService, times(1)).getSubscriptions();
        verify(circuitBreaker, times(2)).execute(any());

        vertxTestContext.completeNow();
      })
      .onFailure(t -> vertxTestContext.failNow("dataChanged call should have succeeded"));
  }

  @Test
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void dataChangedFailedToFetchSubscriptions(VertxTestContext vertxTestContext) {
    when(subscriptionsDatabaseService.getSubscriptions())
      .thenReturn(Future.failedFuture(new ServiceException(500, "Unknown database error.")));

    subscriptionsService.dataChanged(ResourceType.ACTIVITY, true)
      .onSuccess(v -> vertxTestContext.failNow("dataChanged call should NOT have succeeded"))
      .onFailure(t -> vertxTestContext.completeNow());
  }
}

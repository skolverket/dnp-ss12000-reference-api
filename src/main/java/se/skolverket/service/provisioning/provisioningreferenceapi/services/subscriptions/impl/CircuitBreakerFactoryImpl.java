package se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.impl;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.CircuitBreakerFactory;

@Slf4j
public class CircuitBreakerFactoryImpl implements CircuitBreakerFactory {

  // How many attempts after attempt #1 are permitted.
  private int NOTIFICATION_RETRIES = 3;
  // The time a notification is allowed to take before timing out.
  private long NOTIFICATION_TIMEOUT_MS = 5000L;
  // Duration after the first attempt during which retries are permitted.
  private long NOTIFICATION_RETRY_TIMEOUT_MS = 300000L;
  // The time it takes for the circuit breaker to go from an open state to a
  // half-open state in case of reaching the maximum allowed failures (probably
  // due to faulty subscriptions or connectivity problems). NB. a half-open
  // circuit breaker does not allow retries.
  private long CIRCUIT_BREAKER_RESET_TIMEOUT_MS = 1L;

  public CircuitBreakerFactoryImpl() {
  }

  public CircuitBreakerFactoryImpl(int NOTIFICATION_RETRIES, long NOTIFICATION_TIMEOUT_MS, long NOTIFICATION_RETRY_TIMEOUT_MS, long CIRCUIT_BREAKER_RESET_TIMEOUT_MS) {
    this.NOTIFICATION_RETRIES = NOTIFICATION_RETRIES;
    this.NOTIFICATION_TIMEOUT_MS = NOTIFICATION_TIMEOUT_MS;
    this.NOTIFICATION_RETRY_TIMEOUT_MS = NOTIFICATION_RETRY_TIMEOUT_MS;
    this.CIRCUIT_BREAKER_RESET_TIMEOUT_MS = CIRCUIT_BREAKER_RESET_TIMEOUT_MS;
  }

  public CircuitBreaker getCircuitBreaker(Vertx vertx, String id) {
    // Create an instance of SubscriptionsService, passing it the required
    // circuit breaker and web client, used for issuing request to subscribing
    // endpoints.
    CircuitBreaker circuitBreaker = CircuitBreaker.create(
        "subscriptionService-circuitBreaker-" + id, vertx,
        new CircuitBreakerOptions()
          .setMaxRetries(NOTIFICATION_RETRIES)
          .setMaxFailures(NOTIFICATION_RETRIES)
          .setTimeout(NOTIFICATION_TIMEOUT_MS)
          .setResetTimeout(CIRCUIT_BREAKER_RESET_TIMEOUT_MS)
      ).retryPolicy(retryCount -> NOTIFICATION_RETRY_TIMEOUT_MS / NOTIFICATION_RETRIES)
      .closeHandler(v -> log.info("Circuit breaker state is now closed"))
      .openHandler(v -> log.warn("Circuit breaker state is open due to reaching failure limit"))
      .halfOpenHandler(v -> log.info("Circuit breaker state is now half-open after reset timeout"));

    return circuitBreaker;
  }
}

package se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.core.Vertx;

public interface CircuitBreakerFactory {

  CircuitBreaker getCircuitBreaker(Vertx vertx, String id);
}

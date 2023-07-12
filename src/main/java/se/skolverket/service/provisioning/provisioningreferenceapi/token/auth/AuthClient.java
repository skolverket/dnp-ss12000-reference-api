package se.skolverket.service.provisioning.provisioningreferenceapi.token.auth;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import se.skolverket.service.provisioning.provisioningreferenceapi.token.model.AccessToken;

public interface AuthClient {

  Future<AccessToken> authenticateAndGetToken(Vertx vertx);
}

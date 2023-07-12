package se.skolverket.service.provisioning.provisioningreferenceapi.helper;

import io.vertx.core.Future;
import se.skolverket.service.provisioning.provisioningreferenceapi.token.GuardianOfTheTokenService;

public class GuardianOfTheTokenHelperMockService implements GuardianOfTheTokenService {
  @Override
  public Future<String> renewToken() {
    return Future.succeededFuture("ABCD");
  }
}

package se.skolverket.service.provisioning.provisioningreferenceapi.token;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.ProxyIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.SharedData;
import se.skolverket.service.provisioning.provisioningreferenceapi.token.auth.AuthClient;
import se.skolverket.service.provisioning.provisioningreferenceapi.token.impl.GuardianOfTheTokenServiceImpl;

@ProxyGen
@VertxGen
public interface GuardianOfTheTokenService {

  String ADDRESS = "token-service";

  @ProxyIgnore
  @GenIgnore
  static GuardianOfTheTokenService create(Vertx vertx, SharedData sharedData, AuthClient authClient) {
    return new GuardianOfTheTokenServiceImpl(vertx, sharedData, authClient);
  }

  @ProxyIgnore
  @GenIgnore
  static GuardianOfTheTokenService createProxy(Vertx vertx) {
    return new GuardianOfTheTokenServiceVertxEBProxy(vertx, GuardianOfTheTokenService.ADDRESS);
  }

  Future<String> renewToken();

}

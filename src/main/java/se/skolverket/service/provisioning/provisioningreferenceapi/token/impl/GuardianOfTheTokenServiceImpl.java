package se.skolverket.service.provisioning.provisioningreferenceapi.token.impl;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.SharedData;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.token.GuardianOfTheTokenService;
import se.skolverket.service.provisioning.provisioningreferenceapi.token.auth.AuthClient;
import se.skolverket.service.provisioning.provisioningreferenceapi.token.helper.TokenHelper;

@Slf4j
public class GuardianOfTheTokenServiceImpl implements GuardianOfTheTokenService {

  private final Vertx vertx;
  private final AuthClient authClient;
  private AsyncMap<String, String> tokenMap;

  private Long counterId;

  public GuardianOfTheTokenServiceImpl(Vertx vertx, SharedData sharedData, AuthClient authClient) {
    this.vertx = vertx;
    this.authClient = authClient;
    sharedData.<String, String>getLocalAsyncMap(TokenHelper.SHARED_MAP_NAME, event -> {
      if (event.succeeded()) {
        this.tokenMap = event.result();
      } else {
        log.error("Error getting async map.", event.cause());
      }
    });
  }

  @Override
  public Future<String> renewToken() {
    log.info("Renewing token.");
    return authClient.authenticateAndGetToken(vertx)
      .onFailure(throwable -> {
        log.error("Unable to renew token.", throwable);
        if (counterId != null) {
          vertx.cancelTimer(counterId);
        }
        counterId = vertx.setTimer(5000L, l -> renewToken());
      })
      .compose(accessToken -> {
        try {
          // Cancel old timer as to not have multiple tasks running.
          if (counterId != null) {
            vertx.cancelTimer(counterId);
          }
          // Start next timer, multiply seconds by 900 to get milliseconds with margin for renewal.
          counterId = vertx.setTimer(accessToken.getExpiresIn() * 900L, l -> renewToken());
          log.info("Token renewal timer started at {} ms.", accessToken.getExpiresIn() * 900L);
          return tokenMap.put(TokenHelper.TOKEN_MAP_KEY, accessToken.getValue())
            .compose(v -> {
              log.info("Token published to data map.");
              return Future.succeededFuture(accessToken.getValue());
            });
        } catch (NullPointerException e) {
          log.error("Error getting token. AccessToken: {}.", accessToken == null ? "null" : accessToken.toString(), e);
          return Future.failedFuture(e);
        }

      });
  }
}

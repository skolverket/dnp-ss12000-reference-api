package se.skolverket.service.provisioning.provisioningreferenceapi.token.helper;

import io.vertx.core.Future;
import io.vertx.core.shareddata.SharedData;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.token.GuardianOfTheTokenService;

@Slf4j
public class TokenHelper {

  public static final String SHARED_MAP_NAME = "token_store";
  public static final String TOKEN_MAP_KEY = "token";

  /**
   * Helper method to get token from the shared map or renew token.
   * @param sharedData Instance of Vert.x shared data.
   * @param guardianOfTheTokenService Instance of GuardianOfTheTokenService or GuardianOfTheTokenService proxy.
   * @return Future that is succeeded when token has been retrieved either from distributed shared data or a new token from GuardianOfTheTokenService.
   */
  public static Future<String> getToken(SharedData sharedData, GuardianOfTheTokenService guardianOfTheTokenService) {
    return sharedData.<String, String>getLocalAsyncMap(SHARED_MAP_NAME)
      .compose(tokenMap -> tokenMap.get(TOKEN_MAP_KEY))
      .compose(token -> {
        if (token == null || token.isEmpty()) {
          log.warn("Token not found in shared map. Renewing token.");
          return guardianOfTheTokenService.renewToken();
        } else {
          log.debug("Token found in shared map.");
          return Future.succeededFuture(token);
        }
      });
  }
}

package se.skolverket.service.provisioning.provisioningreferenceapi.token.auth.impl;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import se.skolverket.service.provisioning.provisioningreferenceapi.token.auth.AuthClient;
import se.skolverket.service.provisioning.provisioningreferenceapi.token.auth.impl.model.GNAPResponse;
import se.skolverket.service.provisioning.provisioningreferenceapi.token.auth.impl.model.MTLSAuthOptions;
import se.skolverket.service.provisioning.provisioningreferenceapi.token.model.AccessToken;


@Slf4j
public class MTLSAuthClientImpl implements AuthClient {

  private final MTLSAuthOptions mtlsAuthOptions;
  private final HttpClient httpClient;

  public MTLSAuthClientImpl(MTLSAuthOptions mtlsAuthOptions, HttpClient httpClient) {
    this.mtlsAuthOptions = mtlsAuthOptions;
    this.httpClient = httpClient;
  }

  @Override
  public Future<AccessToken> authenticateAndGetToken(Vertx vertx) {
    /*
    * Used for debug, development and testing. See readme for documentation on env variables to disable auth.
    * */
    if (mtlsAuthOptions.authConfig().isAuthDisabled()) {
      log.warn("JWT Auth is disabled.");
      AccessToken accessToken = new AccessToken();
      accessToken.setValue("");
      accessToken.setExpiresIn(60 * 60 * 24L);
      return Future.succeededFuture(accessToken);
    }

    return vertx.executeBlocking(promise -> {
      try {
        HttpPost httpPost = new HttpPost(mtlsAuthOptions.authConfig().getAuthURI());

        httpPost.setEntity(new StringEntity(mtlsAuthOptions.getRequestBody().encode(), ContentType.APPLICATION_JSON));

        HttpResponse response = httpClient.execute(httpPost);
        StatusLine statusLine = response.getStatusLine();
        if (statusLine.getStatusCode() < 300) {
          HttpEntity entity = response.getEntity();
          JsonObject gnapJson = new JsonObject(new String(entity.getContent().readAllBytes()));
          EntityUtils.consume(entity);
          promise.complete(gnapJson.mapTo(GNAPResponse.class).getAccessToken());
        } else {
          log.error("Error getting access token. Status: {}. Body: {}.", statusLine, new String(response.getEntity().getContent().readAllBytes()));
          promise.fail(new ServiceException(statusLine.getStatusCode(), "Error getting access token."));
        }
      } catch (Exception e) {
        log.error("Unable to get Access token.", e);
      }
    });
  }

}

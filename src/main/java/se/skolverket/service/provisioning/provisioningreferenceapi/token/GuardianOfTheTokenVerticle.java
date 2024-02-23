package se.skolverket.service.provisioning.provisioningreferenceapi.token;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.SharedData;
import io.vertx.serviceproxy.ServiceBinder;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import se.skolverket.service.provisioning.provisioningreferenceapi.token.auth.AuthClient;
import se.skolverket.service.provisioning.provisioningreferenceapi.token.auth.impl.MTLSAuthClientImpl;
import se.skolverket.service.provisioning.provisioningreferenceapi.token.auth.impl.model.MTLSAuthOptions;
import se.skolverket.service.provisioning.provisioningreferenceapi.token.model.AuthConfig;

import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.Constants.*;


@Slf4j
public class GuardianOfTheTokenVerticle extends AbstractVerticle {

  private static HttpClient getHttpClientWithSSLContext(AuthConfig authConfig, JsonObject config)
    throws KeyStoreException, IOException, UnrecoverableKeyException, NoSuchAlgorithmException,
    KeyManagementException, CertificateException {
    var httpClientBuilder = getHttpClientBuilder(config);
    KeyStore clientStore = KeyStore.getInstance("PKCS12");
    clientStore.load(new FileInputStream(authConfig.getPkcs12FilePath()), authConfig.getPkcs12Password().toCharArray());
    log.info("Loaded certificate from path: {}", authConfig.getPkcs12FilePath());

    SSLContext sslContext = SSLContextBuilder.create()
      .loadKeyMaterial(clientStore, authConfig.getPkcs12Password().toCharArray(),
        (aliases, socket) -> authConfig.getAlias())
      .build();

    return httpClientBuilder
      .setSSLContext(sslContext)
      .build();
  }

  private static HttpClientBuilder getHttpClientBuilder(JsonObject config) {
    HttpClientBuilder httpClientBuilder = HttpClients.custom();
    if (config.containsKey(CONFIG_HTTP_PROXY_HOST)) {
      HttpHost proxy = new HttpHost(
        config.getString(CONFIG_HTTP_PROXY_HOST),
        config.getInteger(CONFIG_HTTP_PROXY_PORT, CONFIG_HTTP_PROXY_PORT_DEFAULT));
      httpClientBuilder.setProxy(proxy);
    }
    return httpClientBuilder;
  }

  @Override
  public void start(Promise<Void> startPromise) {
    try {
      AuthConfig authConfig = AuthConfig.fromJson(config());

      SharedData sharedData = vertx.sharedData();

      MTLSAuthOptions mtlsAuthOptions = new MTLSAuthOptions()
        .authConfig(authConfig);

      vertx.<HttpClient>executeBlocking(promise -> {
        try {
          promise.complete(getHttpClientWithSSLContext(authConfig, config()));
        } catch (Exception e) {
          promise.fail(e);
        }
      }).compose(httpClient -> {
        AuthClient authClient = new MTLSAuthClientImpl(mtlsAuthOptions, httpClient);

        GuardianOfTheTokenService guardianOfTheTokenService = GuardianOfTheTokenService
          .create(vertx,
            sharedData,
            authClient);

        ServiceBinder binder = new ServiceBinder(vertx);
        binder
          .setAddress(GuardianOfTheTokenService.ADDRESS)
          .register(GuardianOfTheTokenService.class, guardianOfTheTokenService);
        return Future.succeededFuture();
      }).onSuccess(o -> {
        log.info("Token service started.");
        startPromise.complete();
      }).onFailure(startPromise::fail);
    } catch (Exception e) {
      log.error("Unable to load auth config.", e);
      startPromise.fail(e);
    }
  }

}

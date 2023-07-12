package se.skolverket.service.provisioning.provisioningreferenceapi.token;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.SharedData;
import io.vertx.serviceproxy.ServiceBinder;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
public class GuardianOfTheTokenVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) {
    try {
      AuthConfig authConfig = AuthConfig.fromJson(config());

      SharedData sharedData = vertx.sharedData();

      MTLSAuthOptions mtlsAuthOptions = new MTLSAuthOptions()
        .authConfig(authConfig);

      HttpClient httpClient = getHttpClientWithSSLContext(authConfig);

      AuthClient authClient = new MTLSAuthClientImpl(mtlsAuthOptions, httpClient);

      GuardianOfTheTokenService guardianOfTheTokenService = GuardianOfTheTokenService
        .create(vertx,
          sharedData,
          authClient);

      /* POC code for the token service. */
      /*vertx.setPeriodic(10000L, 5000L, aLong -> {
        TokenHelper.getToken(sharedData, GuardianOfTheTokenService.createProxy(vertx))
          .onSuccess(s -> {
            log.info("GOT TOKEN 1 {}", s);
          }).onFailure(throwable -> {
            log.error("Error getting token.", throwable);
          });
      });*/


      ServiceBinder binder = new ServiceBinder(vertx);
      MessageConsumer<JsonObject> consumer = binder
        .setAddress(GuardianOfTheTokenService.ADDRESS)
        .register(GuardianOfTheTokenService.class, guardianOfTheTokenService);

      log.info("Token service started.");
      startPromise.complete();
    } catch (Exception e) {
      log.error("Unable to load auth config.", e);
      startPromise.fail(e);
    }
  }

  static HttpClient getHttpClientWithSSLContext(AuthConfig authConfig)
    throws KeyStoreException, IOException, UnrecoverableKeyException, NoSuchAlgorithmException,
    KeyManagementException, CertificateException {
    var httpClientBuilder = getHttpClientBuilder();
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

  private static HttpClientBuilder getHttpClientBuilder() {
    return HttpClients.custom();
  }

}

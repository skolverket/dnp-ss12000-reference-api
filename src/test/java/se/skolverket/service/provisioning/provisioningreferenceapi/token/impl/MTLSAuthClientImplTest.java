package se.skolverket.service.provisioning.provisioningreferenceapi.token.impl;

import io.vertx.core.Vertx;
import io.vertx.core.http.ClientAuth;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.PfxOptions;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import se.skolverket.service.provisioning.provisioningreferenceapi.token.auth.impl.MTLSAuthClientImpl;
import se.skolverket.service.provisioning.provisioningreferenceapi.token.auth.impl.model.MTLSAuthOptions;
import se.skolverket.service.provisioning.provisioningreferenceapi.token.model.AuthConfig;

import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@ExtendWith(VertxExtension.class)
class MTLSAuthClientImplTest {

  private static final String PKCS_PATH = "src/main/resources/pkcs/ss12k-ref.p12";
  private static final String PKCS_PASSWORD = "Bfv@U4bT5yzL3s7B";

  static HttpClient getHttpClientWithSSLContext(AuthConfig authConfig)
    throws KeyStoreException, IOException, UnrecoverableKeyException, NoSuchAlgorithmException,
    KeyManagementException, CertificateException {
    var httpClientBuilder = getHttpClientBuilder();
    KeyStore clientStore = KeyStore.getInstance("PKCS12");
    clientStore.load(new FileInputStream(authConfig.getPkcs12FilePath()), authConfig.getPkcs12Password().toCharArray());
    log.info("Loaded certificate from path: {}", authConfig.getPkcs12FilePath());

    SSLContext sslContext = SSLContextBuilder.create()
      .loadTrustMaterial(null, (chain, authType) -> true)
      .loadKeyMaterial(clientStore, authConfig.getPkcs12Password().toCharArray(),
        (aliases, socket) -> authConfig.getAlias())
      .build();

    return httpClientBuilder
      .setSSLContext(sslContext)
      .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
      .build();
  }

  private static HttpClientBuilder getHttpClientBuilder() {
    return HttpClients.custom();
  }

  public static Integer findRandomOpenPortOnAllLocalInterfaces() {
    try (
      ServerSocket socket = new ServerSocket(0)
    ) {
      return socket.getLocalPort();
    } catch (Exception e) {
      e.printStackTrace();
      return 8989;
    }
  }

  @Test
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void testGetToken(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);

    HttpServerOptions httpServerOptions = new HttpServerOptions()
      .setPfxTrustOptions(new PfxOptions()
        .setPath(PKCS_PATH)
        .setPassword(PKCS_PASSWORD))
      .setPfxKeyCertOptions(new PfxOptions()
        .setPath(PKCS_PATH)
        .setPassword(PKCS_PASSWORD))
      .setClientAuth(ClientAuth.REQUIRED)
      .setSsl(true);
    Integer port = findRandomOpenPortOnAllLocalInterfaces();
    vertx.createHttpServer(httpServerOptions)
      .requestHandler(httpServerRequest -> testContext.verify(() -> {
        assertNotNull(httpServerRequest.sslSession().getPeerCertificates());
        log.info("Peer certificates:\n {}", Arrays.toString(httpServerRequest.sslSession().getPeerCertificates()));
        assertTrue(httpServerRequest.getHeader("Content-Type").startsWith("application/json"));
        assertNotNull(httpServerRequest.getHeader("content-length"));
        httpServerRequest.response().setStatusCode(200)
          .end("{\"access_token\":" +
            "{\"value\":\"TOKEN\",\"access\":[],\"expires_in\":864000,\"flags\":[\"bearer\"]}}");
        checkpoint.flag();
      })).listen(port, "localhost")
      .onFailure(throwable -> log.error("Error starting test server.", throwable))
      .onSuccess(httpServer -> {
        log.info("Listening for connections.");
        MTLSAuthOptions mtlsAuthOptions = new MTLSAuthOptions()
          .authConfig(new AuthConfig("https://localhost:" + port + "/transaction",
            "https://ss12k-utv.skolverket.se",
            PKCS_PATH,
            PKCS_PASSWORD,
            "ss12k-ref"));

        try {
          HttpClient httpClient = getHttpClientWithSSLContext(mtlsAuthOptions.authConfig());
          MTLSAuthClientImpl authClient = new MTLSAuthClientImpl(mtlsAuthOptions, httpClient);
          authClient.authenticateAndGetToken(vertx)
            .onSuccess(accessToken -> {
              log.info(accessToken.toJson().encode());
              checkpoint.flag();
            }).onFailure(throwable -> {
              log.error("Error", throwable);
              testContext.failNow(throwable);
            });
        } catch (Exception e) {
          testContext.failNow(e);
        }


      });
  }


}

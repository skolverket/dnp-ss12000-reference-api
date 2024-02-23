package se.skolverket.service.provisioning.provisioningreferenceapi.common;

import io.vertx.core.json.JsonObject;
import io.vertx.core.net.ProxyOptions;
import io.vertx.ext.web.client.WebClientOptions;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.Constants.*;

@Slf4j
public class WebClientOptionsWithProxyOptions {
  public static WebClientOptions create(JsonObject config) {
    WebClientOptions webClientOptions = new WebClientOptions();
    if (config.containsKey(CONFIG_HTTP_PROXY_HOST)) {
      webClientOptions.setProxyOptions(
        new ProxyOptions()
          .setHost(config.getString(CONFIG_HTTP_PROXY_HOST))
          .setPort(config.getInteger(CONFIG_HTTP_PROXY_PORT, CONFIG_HTTP_PROXY_PORT_DEFAULT))
      );
      String proxyExceptions = config.getString(CONFIG_HTTP_PROXY_EXCEPTIONS, "");
      List<String> exceptions = Arrays.stream(proxyExceptions.split(",")).filter(s -> !s.isBlank()).collect(Collectors.toList());
      log.info("Proxy exceptions: {}", exceptions);
      webClientOptions.setNonProxyHosts(exceptions);
    }
    if (config.containsKey(CONFIG_HTTP_TRUST_ALL)) {
      try {
        webClientOptions.setTrustAll(Boolean.parseBoolean(config.getString(CONFIG_HTTP_TRUST_ALL, "false")));
      } catch (Exception e) {
        log.warn("Unable to parse {} config. Setting default.", CONFIG_HTTP_TRUST_ALL);
        webClientOptions.setTrustAll(false);
      }
    } else {
      webClientOptions.setTrustAll(false);
    }
    return webClientOptions;
  }
}

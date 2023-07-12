package se.skolverket.service.provisioning.provisioningreferenceapi.token.model;

import io.vertx.core.json.JsonObject;
import lombok.Getter;

import java.util.Objects;

@Getter
public class AuthConfig {
  private final String authURI;

  private final String clientKey;

  private final String pkcs12FilePath;

  private final String pkcs12Password;

  private final String alias;

  private final boolean authDisabled;

  public static AuthConfig fromJson(JsonObject jsonObject) {
    return new AuthConfig(
      jsonObject.getString("AUTH_URI"),
      jsonObject.getString("AUTH_CLIENT_KEY"),
      jsonObject.getString("AUTH_PKCS_PATH"),
      jsonObject.getString("AUTH_PKCS_PASSWORD"),
      jsonObject.getString("AUTH_ALIAS"),
      jsonObject.getBoolean("AUTH_DISABLED", false)
    );
  }

  public AuthConfig(String authURI, String clientKey, String pkcs12FilePath, String pkcs12Password, String alias, boolean authDisabled) {
    this.authURI = Objects.requireNonNull(authURI);
    this.clientKey = Objects.requireNonNull(clientKey);
    this.pkcs12FilePath = Objects.requireNonNull(pkcs12FilePath);
    this.pkcs12Password = Objects.requireNonNull(pkcs12Password);
    this.alias = alias;
    this.authDisabled = authDisabled;
  }

  public AuthConfig(String authURI, String clientKey, String pkcs12FilePath, String pkcs12Password, String alias) {
    this.authURI = authURI;
    this.clientKey = clientKey;
    this.pkcs12FilePath = pkcs12FilePath;
    this.pkcs12Password = pkcs12Password;
    this.alias = alias;
    this.authDisabled = false;
  }

  @Override
  public String toString() {
    return "AuthConfig{" +
      "authURI='" + authURI + '\'' +
      ", clientKey='" + clientKey + '\'' +
      ", pkcs12FilePath='" + pkcs12FilePath + '\'' +
      ", alias='" + alias + '\'' +
      ", pkcs12Password='" + "REDACTED" + '\'' +
      '}';
  }
}

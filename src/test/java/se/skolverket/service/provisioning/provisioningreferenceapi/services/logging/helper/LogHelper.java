package se.skolverket.service.provisioning.provisioningreferenceapi.services.logging.helper;

import se.skolverket.service.provisioning.provisioningreferenceapi.services.logging.model.Log;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.logging.model.SeverityLevel;

import java.util.UUID;

public class LogHelper {
  private LogHelper() {}

  public static Log validLog() {
    return Log.builder()
      .message("Valid Log Message")
      .messageType(UUID.randomUUID().toString())
      .resourceType("Person")
      .resourceId(UUID.randomUUID().toString())
      .resourceUrl("https://test-url.net")
      .severityLevel(SeverityLevel.INFO)
      .build();
  }
}

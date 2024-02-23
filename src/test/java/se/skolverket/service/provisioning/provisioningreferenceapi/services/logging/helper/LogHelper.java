package se.skolverket.service.provisioning.provisioningreferenceapi.services.logging.helper;

import se.skolverket.service.provisioning.provisioningreferenceapi.services.logging.model.LogEntry;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.logging.model.SeverityLevel;

import java.util.UUID;

public class LogHelper {
  private LogHelper() {}

  public static LogEntry validLog() {
    return LogEntry.builder()
      .message("Valid LogEntry Message")
      .messageType(UUID.randomUUID().toString())
      .resourceType("Person")
      .resourceId(UUID.randomUUID().toString())
      .resourceUrl("https://test-url.net")
      .severityLevel(SeverityLevel.INFO)
      .build();
  }
}

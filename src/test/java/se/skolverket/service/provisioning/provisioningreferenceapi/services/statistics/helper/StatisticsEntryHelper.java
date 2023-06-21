package se.skolverket.service.provisioning.provisioningreferenceapi.services.statistics.helper;

import se.skolverket.service.provisioning.provisioningreferenceapi.services.statistics.model.StatisticsEntry;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class StatisticsEntryHelper {
  private StatisticsEntryHelper() {
  }

  public static StatisticsEntry validStatisticsEntry() {
    return StatisticsEntry.builder()
      .description("Description")
      .resourceType("Person")
      .newCount(100)
      .updatedCount(0)
      .deletedCount(0)
      .resourceUrl("localhost/test")
      .timeOfOccurance(ZonedDateTime.now(ZoneOffset.UTC))
      .build();
  }
}

package se.skolverket.service.provisioning.provisioningreferenceapi.services.statistics.impl;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.statistics.StatisticsService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.statistics.database.StatisticsDatabaseService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static se.skolverket.service.provisioning.provisioningreferenceapi.services.statistics.helper.StatisticsEntryHelper.validStatisticsEntry;

@ExtendWith(VertxExtension.class)
class StatisticsServiceImplTest {
  private StatisticsDatabaseService mockStatisticsDatabaseService;
  private StatisticsService statisticsService;

  @BeforeEach
  @DisplayName("StatisticsServiceImpl setup.")
  void setup(VertxTestContext testContext) {
    mockStatisticsDatabaseService = Mockito.mock(StatisticsDatabaseService.class);
    statisticsService = new StatisticsServiceImpl(mockStatisticsDatabaseService);
    testContext.completeNow();
  }


  @Test
  @DisplayName("Get StatisticsEntry")
  void getLogs(VertxTestContext testContext) {
    Mockito.when(mockStatisticsDatabaseService.findStatistics(any())).thenReturn(Future.succeededFuture(List.of(validStatisticsEntry())));
    statisticsService.getStatisticsEntries(new JsonObject())
      .onComplete(testContext.succeeding(strings -> testContext.verify(() -> {
        Mockito.verify(mockStatisticsDatabaseService, Mockito.times(1)).findStatistics(any());
        testContext.completeNow();
      })));
  }

  @Test
  @DisplayName("Create StatisticsEntry")
  void createStatisticsEntrySuccess(VertxTestContext testContext) {
    Mockito.when(mockStatisticsDatabaseService.insertStatisticsEntry(any())).thenReturn(Future.succeededFuture("id"));
    statisticsService.createStatisticsEntry(validStatisticsEntry())
      .onComplete(testContext.succeeding(strings -> testContext.verify(() -> {
        Mockito.verify(mockStatisticsDatabaseService, Mockito.times(1)).insertStatisticsEntry(any());
        testContext.completeNow();
      })));
  }
}

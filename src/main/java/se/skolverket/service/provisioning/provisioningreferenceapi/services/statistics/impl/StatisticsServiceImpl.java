package se.skolverket.service.provisioning.provisioningreferenceapi.services.statistics.impl;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.statistics.StatisticsService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.statistics.database.StatisticsDatabaseService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.statistics.model.StatisticsEntry;

import java.util.List;

public class StatisticsServiceImpl implements StatisticsService {

  private final StatisticsDatabaseService statisticsDatabaseService;

  public StatisticsServiceImpl(StatisticsDatabaseService statisticsDatabaseService) {
    this.statisticsDatabaseService = statisticsDatabaseService;
  }

  @Override
  public Future<String> createStatisticsEntry(StatisticsEntry statisticsEntry) {
    return statisticsDatabaseService.insertStatisticsEntry(statisticsEntry);
  }

  @Override
  public Future<List<StatisticsEntry>> getStatisticsEntries(JsonObject queryOptions) {
    return statisticsDatabaseService.findStatistics(queryOptions);
  }
}

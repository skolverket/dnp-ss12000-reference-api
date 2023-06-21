package se.skolverket.service.provisioning.provisioningreferenceapi.services.statistics.database;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.statistics.model.StatisticsEntry;

import java.util.List;

public interface StatisticsDatabaseService {
  Future<String> insertStatisticsEntry(StatisticsEntry statisticsEntry);
  Future<List<StatisticsEntry>> findStatistics(JsonObject queryOptions);
}

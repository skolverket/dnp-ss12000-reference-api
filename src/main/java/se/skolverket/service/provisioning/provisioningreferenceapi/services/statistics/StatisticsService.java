package se.skolverket.service.provisioning.provisioningreferenceapi.services.statistics;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.ProxyIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.statistics.database.StatisticsDatabaseService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.statistics.impl.StatisticsServiceImpl;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.statistics.model.StatisticsEntry;

import java.util.List;

@ProxyGen
@VertxGen
public interface StatisticsService {
  String ADDRESS = "statistics-service";

  @ProxyIgnore
  @GenIgnore
  static StatisticsService create(StatisticsDatabaseService statisticsDatabaseService) {
    return new StatisticsServiceImpl(statisticsDatabaseService);
  }

  @ProxyIgnore
  @GenIgnore
  static StatisticsService createProxy(Vertx vertx) {
    return new StatisticsServiceVertxEBProxy(vertx, ADDRESS);
  }

  Future<String> createStatisticsEntry(StatisticsEntry statisticsEntry);

  Future<List<StatisticsEntry>> getStatisticsEntries(JsonObject queryOptions);
}

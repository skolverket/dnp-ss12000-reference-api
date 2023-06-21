package se.skolverket.service.provisioning.provisioningreferenceapi.services.statistics.database.impl;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.AbstractMetaDatabaseService;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.DatabaseServiceHelper;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.statistics.database.StatisticsDatabaseService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.statistics.model.StatisticsEntry;

import java.util.List;
import java.util.stream.Collectors;

import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.Constants.BSON_ID;

@Slf4j
public class StatisticsDatabaseServiceImpl extends AbstractMetaDatabaseService implements StatisticsDatabaseService {

  private final String COLLECTION_NAME = "statistics";

  private final MongoClient mongoClient;

  public StatisticsDatabaseServiceImpl(MongoClient mongoClient) {
    this.mongoClient = mongoClient;
  }

  @Override
  public Future<String> insertStatisticsEntry(StatisticsEntry statisticsEntry) {
    log.info("Saving {}.", statisticsEntry.getClass().getSimpleName());
    return mongoClient.insert(COLLECTION_NAME, statisticsEntry.toBson())
      .compose(s -> {
        log.info("{} saved, with id {}.", statisticsEntry.getClass().getSimpleName(), s);
        return Future.succeededFuture(s);
      })
      .recover(DatabaseServiceHelper::errorHandler);
  }

  @Override
  public Future<List<StatisticsEntry>> findStatistics(JsonObject queryOptions) {
    JsonObject query = buildQuery(queryOptions);
    return mongoClient.findWithOptions(COLLECTION_NAME, query,
      new FindOptions().setSort(new JsonObject().put(BSON_ID, -1)))
      .compose(jsonObjects -> Future.succeededFuture(jsonObjects.stream().map(StatisticsEntry::fromBson).collect(Collectors.toList())))
      .recover(DatabaseServiceHelper::errorHandler);
  }
}

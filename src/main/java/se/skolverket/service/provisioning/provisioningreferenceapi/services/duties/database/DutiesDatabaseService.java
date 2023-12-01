package se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.database;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.model.Duty;

import java.util.List;

public interface DutiesDatabaseService {
  Future<List<String>> insertDuties(List<Duty> duties);

  Future<List<String>> deleteDuties(List<Duty> duties);

  Future<List<String>> saveDuties(List<Duty> duties);

  Future<List<Duty>> findDuties(JsonObject queryParams);

  Future<ReadStream<JsonObject>> findDutiesStream(JsonObject queryOptions);

  Future<List<Duty>> findDutiesByDutyIds(List<String> dutyIds);
}

package se.skolverket.service.provisioning.provisioningreferenceapi.helper;

import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.BsonConverterHelper;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("deprecation")
public class BsonConverterHelperTest {

  @Test
  void convertMetaToJsonTest() throws Exception {
    String file = "src/test/resources/sampledata/meta_bson.json";
    String json = readFileToString(file);
    JsonObject metaBson = new JsonObject(json);
    JsonObject metaJson = BsonConverterHelper.convertMetaToJson(metaBson);

    assertEquals("2022-09-28T13:08:30.81Z", metaJson.getJsonObject("meta").getString("created"));
    assertEquals("2022-09-29T13:08:30.81Z", metaJson.getJsonObject("meta").getString("modified"));
  }

  @Test
  void convertStarEndDateToJsonTest() throws Exception {
    String file = "src/test/resources/sampledata/dates_bson.json";
    String json = readFileToString(file);
    JsonObject datesBson = new JsonObject(json);
    JsonObject datesJson = BsonConverterHelper.convertStarEndDateToJson(datesBson);


    assertEquals("2022-07-10", datesJson.getString("startDate"));
    assertEquals("2022-07-11", datesJson.getString("endDate"));
  }

  private static String readFileToString(String file)throws Exception {
    return new String(Files.readAllBytes(Paths.get(file)));
  }
}

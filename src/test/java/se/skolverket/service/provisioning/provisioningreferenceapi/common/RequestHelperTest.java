package se.skolverket.service.provisioning.provisioningreferenceapi.common;

import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.RequestHelper;


import static org.junit.jupiter.api.Assertions.*;
import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.Constants.*;

public class RequestHelperTest {

  /*
  {
    "req": {},
    "cursor": {
        "index": "uuid",
        "limit": 666
    }
  }
   */
  private static final String BASE_64_SAMPLE_PAGE_TOKEN = "ewogICAgInJlcSI6IHt9LAogICAgImN1cnNvciI6IHsKICAgICAgICAiaW5kZXgiOiAidXVpZCIsCiAgICAgICAgImxpbWl0IjogNjY2CiAgICB9Cn0=";

  @Test
  void queryParamsValidSuccess() {
    MultiMap queryParams = MultiMap.caseInsensitiveMultiMap();
    queryParams.set(QP_LIMIT, "10");
    queryParams.set(QP_META_CREATED_AFTER, "2005-01-01");

    assertTrue(RequestHelper.queryParamsValid(queryParams));
  }

  @Test
  void queryParamsValidInvalid() {
    MultiMap queryParams = MultiMap.caseInsensitiveMultiMap();
    queryParams.set(QP_PAGE_TOKEN, "base64stuff");
    queryParams.set(QP_META_CREATED_AFTER, "2005-01-01");

    assertFalse(RequestHelper.queryParamsValid(queryParams));
  }

  @Test
  void parseQueryOptionsPageToken() {
    MultiMap queryParams = MultiMap.caseInsensitiveMultiMap();
    queryParams.set(QP_PAGE_TOKEN, BASE_64_SAMPLE_PAGE_TOKEN);

    JsonObject queryOptions = RequestHelper.parseQueryOptions(queryParams);
    assertEquals(666, queryOptions.getJsonObject("cursor").getInteger("limit"));
    assertEquals("uuid", queryOptions.getJsonObject("cursor").getString("index"));
    assertTrue(queryOptions.getJsonObject("req").isEmpty());
  }

  @Test
  void parseQueryOptionsNoPageToken() {
    MultiMap queryParams = MultiMap.caseInsensitiveMultiMap();
    queryParams.set(QP_LIMIT, "666");
    queryParams.set(QP_META_MODIFIED_AFTER, "2005-01-01T12:13:13.000");

    JsonObject queryOptions = RequestHelper.parseQueryOptions(queryParams);
    assertEquals(666, queryOptions.getJsonObject("cursor").getInteger("limit"));
    assertEquals("2005-01-01T12:13:13.000", queryOptions.getJsonObject("req").getString("meta.modified.after"));
  }
}

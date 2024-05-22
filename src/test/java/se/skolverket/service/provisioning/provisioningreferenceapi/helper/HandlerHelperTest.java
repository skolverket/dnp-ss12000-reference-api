package se.skolverket.service.provisioning.provisioningreferenceapi.helper;

import io.vertx.core.json.JsonArray;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.HandlerHelper.isLastPage;

public class HandlerHelperTest {

  @Test
  void testIsLastPage() {
    /* is NOT last page */
    // Returned # of elements == limit
    assertFalse(isLastPage(new JsonArray().add(1).add(2).add(3).size(), 2, 3));

    /* is last page */
    // Returned # of elements < limit
    assertTrue(isLastPage(new JsonArray().add(1).add(2).size(), 1, 3));
    // Limit == 0
    assertTrue(isLastPage(new JsonArray().add(1).add(2).add(3).size(), 0, 0));
    // Limit == -1
    assertTrue(isLastPage(new JsonArray().add(1).add(2).add(3).size(), 0, -1));
    // Result array was empty -> last position < 0
    assertTrue(isLastPage(new JsonArray().size(), -1, 100));
  }
}

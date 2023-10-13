package se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.model;


import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.DutyAssignment;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.ObjectReference;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.OrganisationReference;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.helper.ActivitiesHelper;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
public class ActivityTest {

  @Test
  @DisplayName("Activity from Json")
  void jsonToActivityTest(Vertx vertx, VertxTestContext testContext) {
    testContext.verify(() -> {
      Buffer buffer = vertx.fileSystem().readFileBlocking(Objects.requireNonNull(getClass().getClassLoader().getResource("sampledata/activity.json")).getPath());
      JsonObject activityJson = new JsonObject(buffer);
      Activity activity = new Activity(activityJson);
      assertEquals("3fa85f64-5717-4562-b3fc-2c963f66afa7", activity.getId());
      assertEquals("string", activity.getDisplayName());
      assertEquals("Undervisning", activity.getActivityType());
      assertEquals(List.of(new ObjectReference("3fa85f64-5717-4562-b3fc-2c963f66afa5", null)), activity.getGroups());
      assertEquals(1, activity.getTeachers().size());
      DutyAssignment teacher = activity.getTeachers().get(0);
      assertEquals(new ObjectReference("3fa85f64-5717-4562-b3fc-2c963f66afa5", null), teacher.getDuty());
      assertEquals(new OrganisationReference("3fa85f64-5717-4562-b3fc-2c963f66afa5", null), activity.getOrganisation());
      assertEquals(new ObjectReference("3fa85f64-5717-4562-b3fc-2c963f66afa5", null), activity.getParentActivity());
      testContext.completeNow();
    });
  }

  @Test
  @DisplayName("Activity from BSON")
  void bsonToActivityTest(Vertx vertx, VertxTestContext testContext) {
    testContext.verify(() -> {
      Activity.fromBson(ActivitiesHelper.validActivityBson(vertx));
      testContext.completeNow();
    });
  }
}

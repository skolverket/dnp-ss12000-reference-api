package se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.helper;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.DutyAssignment;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.ObjectReference;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.OrganisationReference;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.model.Activity;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ActivitiesHelper {

  private ActivitiesHelper() {
  }

  public static Activity validActivity() {
    Activity activity = new Activity();
    activity.setId(UUID.randomUUID().toString());
    activity.setDisplayName("Test Activity display name");
    activity.setStartDate(LocalDate.now());
    activity.setEndDate(LocalDate.now().plusDays(1));
    activity.setActivityType("Test activity");
    activity.setGroups(List.of(new ObjectReference(UUID.randomUUID().toString(), "Test group")));
    activity.setTeachers(
      List.of(
        DutyAssignment.builder()
          .startDate(LocalDate.now())
          .endDate(LocalDate.now().plusDays(90L))
          .duty(new ObjectReference(UUID.randomUUID().toString(), "Test duty"))
          .build()
      )
    );
    OrganisationReference orgRef = new OrganisationReference(UUID.randomUUID().toString(), "orgRefTest");
    activity.setOrganisation(orgRef);
    activity.setParentActivity(new ObjectReference(UUID.randomUUID().toString(), "Esperanto exam"));

    return activity;
  }
  public static JsonObject validActivityBson(Vertx vertx) {
    Buffer buffer = vertx.fileSystem().readFileBlocking(Objects.requireNonNull(ActivitiesHelper.class.getClassLoader().getResource("sampledata/activity_bson.json")).getPath());
    return new JsonObject(buffer);
  }
}

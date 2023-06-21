package se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.testdata;

import io.vertx.core.json.JsonObject;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.OrganisationReference;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.PersonReference;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.model.Duty;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class DutiesTestData {

  private DutiesTestData() {
  }

  public static Duty createInvalidDuty() {
    Duty duty = createValidDuty();
    duty.setId(null);

    return duty;
  }

  public static Duty createValidDuty() {
    PersonReference person = new PersonReference("person_id", "person_display_name");
    OrganisationReference orgRef = new OrganisationReference("org_ref_id", "org_ref_name");
    Duty duty = Duty.builder()
      .person(person)
      .dutyAt(orgRef)
      .dutyRole("Rektor")
      .endDate(LocalDate.now().plusYears(1L))
      .startDate(LocalDate.now())
      .build();
    duty.setId(UUID.randomUUID().toString());
    return duty;
  }

  public static JsonObject createValidDutyBson() {
    return new JsonObject()
      .put("_id", "1")
      .put("startDate", new JsonObject().put("$date", Instant.now().toString()))
      .put("person", new JsonObject().put("id", "a"))
      .put("dutyAt", new JsonObject().put("id", "b"))
      .put("dutyRole", "Rektor");
  }
}

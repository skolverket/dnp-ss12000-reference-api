package se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.testdata;

import io.vertx.core.json.JsonObject;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.OrganisationReference;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.PersonReference;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.model.Duty;

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
      .build();
    duty.setId(UUID.randomUUID().toString());
    return duty;
  }

  public static JsonObject createValidDutyBson() {
    return new JsonObject()
      .put("_id", "1")
      .put("person", new JsonObject().put("id", "a"))
      .put("dutyAt", new JsonObject().put("id", "b"))
      .put("dutyRole", "Rektor");
  }
}

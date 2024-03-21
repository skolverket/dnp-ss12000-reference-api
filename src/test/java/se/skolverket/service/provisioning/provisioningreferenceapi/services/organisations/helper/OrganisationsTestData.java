package se.skolverket.service.provisioning.provisioningreferenceapi.services.organisations.helper;

import io.vertx.core.json.JsonObject;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.organisations.model.Organisation;

import java.util.UUID;

public class OrganisationsTestData {

  public static Organisation createValidOrganisation() {
    return new Organisation(new JsonObject("""
      {
        "id": "%s",
        "displayName": "Test Organisation",
        "schoolUnitCode": "schoolUnitCode"
      }""".formatted(UUID.randomUUID().toString())));
  }
  public static Organisation createInvalidOrganisation() {
    return new Organisation(new JsonObject("""
      {
        "id": "abc",
        "displayName": "Test Organisation",
        "schoolUnitCode": "schoolUnitCode"
      }"""));
  }
}

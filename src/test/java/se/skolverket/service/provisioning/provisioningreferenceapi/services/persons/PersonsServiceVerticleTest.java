package se.skolverket.service.provisioning.provisioningreferenceapi.services.persons;


import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxTestContext;
import io.vertx.servicediscovery.ServiceDiscovery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.skolverket.service.provisioning.provisioningreferenceapi.ProvisioningReferenceApiAbstractTest;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.Enrolment;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.ObjectReference;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.SchoolUnitReference;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.model.Person;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.helper.PersonHelper.validPerson;


class PersonsServiceVerticleTest extends ProvisioningReferenceApiAbstractTest {

  private ServiceDiscovery serviceDiscovery;
  private Integer port;

  @BeforeEach
  @DisplayName("Deploy a verticle")
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    serviceDiscovery = ServiceDiscovery.create(vertx);
    System.err.println("Got Service discovery.");
    port = findRandomOpenPortOnAllLocalInterfaces();
    vertx.deployVerticle(PersonsServiceVerticle.class.getName(), new DeploymentOptions().setConfig(new JsonObject().put("port", port)))
      .onComplete(testContext.succeedingThenComplete());
  }


  @Test
  @DisplayName("Registered to cluster.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void getRecord(VertxTestContext testContext) {
    serviceDiscovery.getRecord(new JsonObject().put("name", PersonsServiceVerticle.SERVICE_NAME).put("ingest", true), testContext.succeeding(record -> testContext.verify(() -> {
      assertNotNull(record);
      assertEquals(PersonsServiceVerticle.SERVICE_NAME, record.getName());
      testContext.completeNow();
    })));
  }

  @Test
  @DisplayName("Validation - Bad request - No person.id.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void badRequestNoPersonId(Vertx vertx, VertxTestContext testContext) {
    JsonObject personJson = validPerson().toJson();
    personJson.remove("id");
    WebClient client = WebClient.create(vertx);
    client.post(port, "localhost", "/")
      .sendJsonObject(new JsonObject().put("persons", new JsonArray().add(personJson)))
      .onComplete(
        testContext.succeeding(bufferHttpResponse ->
          testContext.verify(() -> {
            assertEquals(400, bufferHttpResponse.statusCode());
            testContext.completeNow();
          })
        )
      );
  }
  @Test
  @DisplayName("Validation - Bad request - Bad person.id.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void badRequestBadPersonId(Vertx vertx, VertxTestContext testContext) {
    JsonObject personJson = validPerson().toJson();
    personJson.put("id", "abcd");
    WebClient client = WebClient.create(vertx);
    client.post(port, "localhost", "/")
      .sendJsonObject(new JsonObject().put("persons", new JsonArray().add(personJson)))
      .onComplete(
        testContext.succeeding(bufferHttpResponse ->
          testContext.verify(() -> {
            assertEquals(400, bufferHttpResponse.statusCode());
            testContext.completeNow();
          })
        )
      );
  }

  @Test
  @DisplayName("Validation - Bad request - Empty EPPN array")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void badRequestNoEppn(Vertx vertx, VertxTestContext testContext) {
    Person person = validPerson();
    person.setEduPersonPrincipalNames(List.of());
    WebClient client = WebClient.create(vertx);
    client.post(port, "localhost", "/")
      .sendJsonObject(new JsonObject().put("persons", new JsonArray().add(person.toJson())))
      .onComplete(
        testContext.succeeding(bufferHttpResponse ->
          testContext.verify(() -> {
            assertEquals(400, bufferHttpResponse.statusCode());
            testContext.completeNow();
          })
        )
      );
  }


  @Test
  @DisplayName("Validation - Bad request - No school year for GR")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void badRequestNoSchoolYear(Vertx vertx, VertxTestContext testContext) {
    Person person = validPerson();
    person.setEnrolments( List.of(new Enrolment(
      new SchoolUnitReference(UUID.randomUUID().toString(), null), null, "GR",
      LocalDate.now(),
      LocalDate.now().plusYears(1),
      false, null,
      new ObjectReference(UUID.randomUUID().toString(), "programme_name"),
      "Additional information regarding the education")
    ));
    WebClient client = WebClient.create(vertx);
    client.post(port, "localhost", "/")
      .sendJsonObject(new JsonObject().put("persons", new JsonArray().add(person.toJson())))
      .onComplete(
        testContext.succeeding(bufferHttpResponse ->
          testContext.verify(() -> {
            assertEquals(400, bufferHttpResponse.statusCode());
            testContext.completeNow();
          })
        )
      );
  }


  @Test
  @DisplayName("Validation - Bad request - No educationCode year for GY")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void badRequestNoEducationCode(Vertx vertx, VertxTestContext testContext) {
    Person person = validPerson();
    person.setEnrolments( List.of(new Enrolment(
      new SchoolUnitReference(UUID.randomUUID().toString(), null), null, "GY",
      LocalDate.now(),
      LocalDate.now().plusYears(1),
      false, null,
      new ObjectReference(UUID.randomUUID().toString(), "programme_name"),
      "Additional information regarding the education")
    ));
    WebClient client = WebClient.create(vertx);
    client.post(port, "localhost", "/")
      .sendJsonObject(new JsonObject().put("persons", new JsonArray().add(person.toJson())))
      .onComplete(
        testContext.succeeding(bufferHttpResponse ->
          testContext.verify(() -> {
            assertEquals(400, bufferHttpResponse.statusCode());
            testContext.completeNow();
          })
        )
      );
  }
}

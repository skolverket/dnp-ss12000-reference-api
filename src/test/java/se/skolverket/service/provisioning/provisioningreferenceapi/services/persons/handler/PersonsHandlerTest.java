package se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.handler;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import se.skolverket.service.provisioning.provisioningreferenceapi.helper.AbstractRestApiHelper;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.PersonsService;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.helper.PersonHelper.*;

class PersonsHandlerTest extends AbstractRestApiHelper {

  private PersonsService personsService;

  private static final String BASE_URI = "/persons";

  @BeforeEach
  @DisplayName("PersonsHandlerTest setup.")
  void setup(Vertx vertx, VertxTestContext testContext) {
    personsService = Mockito.mock(PersonsService.class);
    testContext.completeNow();
  }

  @Test
  @DisplayName("Get persons to handler.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void getPersons(Vertx vertx, VertxTestContext testContext) {
    Mockito.when(personsService.getPersons(any())).thenReturn(Future.succeededFuture(List.of(validPerson())));

    mockServer(vertx, HttpMethod.GET, BASE_URI, PersonsHandler.getPersons(personsService), testContext)
      .onComplete(testContext.succeeding(port -> {
        WebClient client = WebClient.create(vertx);
        client.get(port, "localhost", BASE_URI)
          .send()
          .onComplete(
            testContext.succeeding(bufferHttpResponse ->
              testContext.verify(() -> {
                assertEquals(200, bufferHttpResponse.statusCode());
                testContext.completeNow();
              })
            )
          );
      }));
  }

  @Test
  @DisplayName("Post persons to handler.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void postPersons(Vertx vertx, VertxTestContext testContext) {
    Mockito.when(personsService.createPersons(any())).thenReturn(Future.succeededFuture(List.of(UUID.randomUUID().toString())));
    mockServer(vertx, HttpMethod.POST, BASE_URI, PersonsHandler.postPersons(personsService), testContext)
      .onComplete(testContext.succeeding(port -> {
        WebClient client = WebClient.create(vertx);
        client.post(port, "localhost", BASE_URI)
          .sendJsonObject(new JsonObject().put("persons", new JsonArray().add(validPerson().toJson())))
          .onComplete(
            testContext.succeeding(bufferHttpResponse ->
              testContext.verify(() -> {
                assertEquals(201, bufferHttpResponse.statusCode());
                testContext.completeNow();
              })
            )
          );
      }));
  }

  @Test
  @DisplayName("Put persons to handler.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void putPersons(Vertx vertx, VertxTestContext testContext) {
    Mockito.when(personsService.createPersons(any())).thenReturn(Future.succeededFuture(List.of(UUID.randomUUID().toString())));
    mockServer(vertx, HttpMethod.PUT, BASE_URI, PersonsHandler.postPersons(personsService), testContext)
      .onComplete(testContext.succeeding(port -> {
        WebClient client = WebClient.create(vertx);
        client.put(port, "localhost", BASE_URI)
          .sendJsonObject(new JsonObject().put("persons", new JsonArray().add(validPerson().toJson())))
          .onComplete(
            testContext.succeeding(bufferHttpResponse ->
              testContext.verify(() -> {
                assertEquals(201, bufferHttpResponse.statusCode());
                testContext.completeNow();
              })
            )
          );
      }));
  }

  @Test
  @DisplayName("Delete persons to handler.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void deletePersonsSuccessTest(Vertx vertx, VertxTestContext testContext) {
    Mockito.when(personsService.deletePersons(any())).thenReturn(Future.succeededFuture());

    mockServer(vertx, HttpMethod.POST, BASE_URI, PersonsHandler.deletePersons(personsService), testContext)
      .onComplete(testContext.succeeding(ar -> {
        WebClient client = WebClient.create(vertx);
        client.post(ar, "localhost", BASE_URI)
          .sendJsonObject(new JsonObject().put("persons", new JsonArray().add(validPerson())))
          .onComplete(
            testContext.succeeding(response -> testContext.verify(() -> {
              assertEquals(202, response.statusCode());
              testContext.completeNow();
            })));
      }));
  }
}

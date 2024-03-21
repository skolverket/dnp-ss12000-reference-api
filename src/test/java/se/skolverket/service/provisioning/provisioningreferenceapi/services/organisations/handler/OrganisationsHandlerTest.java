package se.skolverket.service.provisioning.provisioningreferenceapi.services.organisations.handler;

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
import se.skolverket.service.provisioning.provisioningreferenceapi.common.StreamingService;
import se.skolverket.service.provisioning.provisioningreferenceapi.helper.AbstractRestApiHelper;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.organisations.OrganisationsService;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static se.skolverket.service.provisioning.provisioningreferenceapi.services.organisations.helper.OrganisationsTestData.createValidOrganisation;

class OrganisationsHandlerTest extends AbstractRestApiHelper {
  private static final String URI = "/organisations";

  private OrganisationsService organisationsService;

  @BeforeEach
  @DisplayName("OrganisationsHandlerTest setup.")
  void setup(VertxTestContext testContext) {
    organisationsService = Mockito.mock(OrganisationsService.class);
    testContext.completeNow();
  }

  @Test
  @DisplayName("Post organisations to handler.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void postOrganisationsSuccessTest(Vertx vertx, VertxTestContext testContext) {

    Mockito.when(organisationsService.createOrganisations(any()))
      .thenReturn(Future.succeededFuture(List.of(UUID.randomUUID().toString())));

    mockServer(vertx, HttpMethod.POST, URI, OrganisationsHandler.post(organisationsService), testContext)
      .onComplete(testContext.succeeding(ar -> {
        WebClient client = WebClient.create(vertx);
        client.post(ar, "localhost", URI)
          .sendJsonObject(new JsonObject().put("organisations", new JsonArray().add(createValidOrganisation().toJson())))
          .onComplete(
            testContext.succeeding(response -> testContext.verify(() -> {
              assertEquals(201, response.statusCode());
              testContext.completeNow();
            })));
      }));
  }

  @Test
  @DisplayName("Put organisations to handler.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void putOrganisationsSuccessTest(Vertx vertx, VertxTestContext testContext) {

    Mockito.when(organisationsService.updateOrganisations(any()))
      .thenReturn(Future.succeededFuture(List.of(UUID.randomUUID().toString())));

    mockServer(vertx, HttpMethod.PUT, URI, OrganisationsHandler.put(organisationsService), testContext)
      .onComplete(testContext.succeeding(ar -> {
        WebClient client = WebClient.create(vertx);
        client.put(ar, "localhost", URI)
          .sendJsonObject(new JsonObject().put("organisations", new JsonArray().add(createValidOrganisation().toJson())))
          .onComplete(
            testContext.succeeding(response -> testContext.verify(() -> {
              assertEquals(201, response.statusCode());
              testContext.completeNow();
            })));
      }));
  }

  @Test
  @DisplayName("Delete organisations.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void deleteOrganisationsSuccessTest(Vertx vertx, VertxTestContext testContext) {
    Mockito.when(organisationsService.deleteOrganisations(any())).thenReturn(Future.succeededFuture());

    mockServer(vertx, HttpMethod.POST, URI, OrganisationsHandler.delete(organisationsService), testContext)
      .onComplete(testContext.succeeding(ar -> {
        WebClient client = WebClient.create(vertx);
        client.post(ar, "localhost", URI)
          .sendJsonObject(new JsonObject().put("organisations", new JsonArray().add(createValidOrganisation().toJson())))
          .onComplete(
            testContext.succeeding(response -> testContext.verify(() -> {
              assertEquals(202, response.statusCode());
              testContext.completeNow();
            })));
      }));
  }

  @Test
  @DisplayName("GET organisations")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void getOrganisationsSuccessTest(Vertx vertx, VertxTestContext testContext) {
    StreamingService streamingService = Mockito.mock(StreamingService.class);
    Mockito.when(streamingService.getStream(any(), any())).thenReturn(Future.succeededFuture());

    mockServer(vertx, HttpMethod.GET, URI, OrganisationsHandler.get(streamingService), testContext)
      .onComplete(testContext.succeeding(port -> {
        WebClient client = WebClient.create(vertx);
        client.get(port, "localhost", URI)
          .send()
          .onComplete(
            testContext.succeeding(bufferHttpResponse ->
              testContext.verify(() -> {
                assertEquals(200, bufferHttpResponse.statusCode());
                testContext.completeNow();
              }))
          );
      }));
  }
}

package se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.handler;

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
import se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.GroupsService;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.testdata.GroupTestData.createValidGroup;

class GroupsHandlerTest extends AbstractRestApiHelper {

  private static final String PATH = "/groups";

  private GroupsService groupsService;

  @BeforeEach
  @DisplayName("GroupsHandlerTest setup.")
  void setup(VertxTestContext testContext) {
    groupsService = Mockito.mock(GroupsService.class);
    testContext.completeNow();
  }

  @Test
  @DisplayName("Get groups to handler.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void getGroups(Vertx vertx, VertxTestContext testContext) {
    Mockito.when(groupsService.getGroups(any())).thenReturn(Future.succeededFuture(List.of(createValidGroup())));

    mockServer(vertx, HttpMethod.GET, PATH, GroupsHandler.getGroups(groupsService), testContext)
      .onComplete(testContext.succeeding(port -> {
        WebClient client = WebClient.create(vertx);
        client.get(port, "localhost", PATH)
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
  @DisplayName("Post groups to handler.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void postGroupsSuccessTest(Vertx vertx, VertxTestContext testContext) {

    Mockito.when(groupsService.createGroups(any()))
      .thenReturn(Future.succeededFuture(List.of(UUID.randomUUID().toString())));

    mockServer(vertx, HttpMethod.POST, PATH, GroupsHandler.postGroups(groupsService), testContext)
      .onComplete(testContext.succeeding(ar -> {
        WebClient client = WebClient.create(vertx);
        client.post(ar, "localhost", PATH)
          .sendJsonObject(new JsonObject().put("groups", new JsonArray().add(createValidGroup().toJson())))
          .onComplete(
            testContext.succeeding(response -> testContext.verify(() -> {
              assertEquals(201, response.statusCode());
              testContext.completeNow();
            })));
      }));
  }

  @Test
  @DisplayName("Put groups handler.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void putGroupsSuccessTest(Vertx vertx, VertxTestContext testContext) {
    Mockito.when(groupsService.updateGroups(any()))
      .thenReturn(Future.succeededFuture(List.of(UUID.randomUUID().toString())));

    mockServer(vertx, HttpMethod.PUT, PATH, GroupsHandler.putGroups(groupsService), testContext)
      .onComplete(testContext.succeeding(ar -> {
        WebClient webClient = WebClient.create(vertx);
        webClient.put(ar, "localhost", PATH)
          .sendJsonObject(new JsonObject().put("groups", new JsonArray().add(createValidGroup().toJson())))
          .onComplete(
            testContext.succeeding(response -> testContext.verify(() -> {
              assertEquals(201, response.statusCode());
              testContext.completeNow();
            })));
      }));
  }

  @Test
  @DisplayName("Delete groups to handler.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void deleteGroupsSuccessTest(Vertx vertx, VertxTestContext testContext) {
    Mockito.when(groupsService.deleteGroups(any())).thenReturn(Future.succeededFuture());

    mockServer(vertx, HttpMethod.POST, PATH, GroupsHandler.deleteGroups(groupsService), testContext)
      .onComplete(testContext.succeeding(ar -> {
        WebClient client = WebClient.create(vertx);
        client.post(ar, "localhost", PATH)
          .sendJsonObject(new JsonObject().put("groups", new JsonArray().add(createValidGroup().toJson())))
          .onComplete(
            testContext.succeeding(response -> testContext.verify(() -> {
              assertEquals(202, response.statusCode());
              testContext.completeNow();
            })));
      }));
  }
}

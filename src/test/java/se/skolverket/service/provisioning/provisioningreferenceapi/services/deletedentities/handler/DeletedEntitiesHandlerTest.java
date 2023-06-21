package se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.handler;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.ResourceType;
import se.skolverket.service.provisioning.provisioningreferenceapi.helper.AbstractRestApiHelper;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.DeletedEntitiesService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.model.DeletedEntity;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

public class DeletedEntitiesHandlerTest extends AbstractRestApiHelper {

  private DeletedEntitiesService deletedEntitiesService;

  private static final String BASE_URI = "/deletedEntities";

  @BeforeEach
  @DisplayName("DeletedEntitiesHandlerTest setup.")
  void setup(VertxTestContext testContext) {
    deletedEntitiesService = Mockito.mock(DeletedEntitiesService.class);
    testContext.completeNow();
  }

  @Test
  @DisplayName("Get deletedEntities to handler.")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void getDeletedEntities(Vertx vertx, VertxTestContext testContext) {
    Mockito.when(deletedEntitiesService.getEntities(any())).thenReturn(Future.succeededFuture(
      List.of(new DeletedEntity("123456789", "987654321", ResourceType.PERSON))));

    mockServer(vertx, HttpMethod.GET, BASE_URI, DeletedEntitiesHandler.getDeletedEntities(deletedEntitiesService), testContext)
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
}

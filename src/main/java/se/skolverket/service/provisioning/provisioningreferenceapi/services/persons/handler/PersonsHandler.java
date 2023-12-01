package se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.handler;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.StreamingService;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.SimpleResponseBody;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.PersonsService;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.model.Person;

import java.util.List;

import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.HandlerHelper.getBodyAndParse;
import static se.skolverket.service.provisioning.provisioningreferenceapi.common.helper.RequestHelper.*;

@Slf4j
public class PersonsHandler {

  private static final String KEY = "persons";

  public static Handler<RoutingContext> postPersons(PersonsService personsService) {
    return routingContext -> {
      List<Person> persons = getBodyAndParse(routingContext, Person.class, KEY);
      personsService.createPersons(persons)
        .onFailure(routingContext::fail)
        .onSuccess(strings -> response201Json(routingContext, new SimpleResponseBody(strings)));
    };
  }

  public static Handler<RoutingContext> getPersons(StreamingService streamingService) {
    return routingContext -> {
      HttpServerResponse response = routingContext.response();
      response.setChunked(true);
      try {
        JsonObject queryOptions = parseQueryOptions(routingContext.request().params());
        streamingService.getStream(response, queryOptions)
          .onFailure(routingContext::fail)
          .onSuccess(v -> response.end());
      } catch (Exception e) {
        response400Error(routingContext);
      }
    };
  }

  public static Handler<RoutingContext> putPersons(PersonsService personsService) {
    return routingContext -> {
      List<Person> persons = getBodyAndParse(routingContext, Person.class, KEY);
      personsService.updatePersons(persons)
        .onFailure(routingContext::fail)
        .onSuccess(strings -> {
          if (strings.isEmpty()) {
            response204(routingContext);
          } else {
            response201Json(routingContext, new SimpleResponseBody(strings));
          }
        });
    };
  }

  public static Handler<RoutingContext> deletePersons(PersonsService personsService) {
    return routingContext -> {
      List<Person> persons = getBodyAndParse(routingContext, Person.class, KEY);
      personsService.deletePersons(persons)
        .onFailure(throwable -> routingContext.fail(500, throwable))
        .onSuccess(v -> response202Json(routingContext, new SimpleResponseBody("message", "Accepted")));
    };
  }
}

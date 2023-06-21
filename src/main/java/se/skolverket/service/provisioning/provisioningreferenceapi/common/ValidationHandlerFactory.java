package se.skolverket.service.provisioning.provisioningreferenceapi.common;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;
import io.vertx.json.schema.OutputUnit;
import io.vertx.json.schema.Validator;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.exception.ValidationFailedException;

import java.util.List;
import java.util.Objects;

@Slf4j
public class ValidationHandlerFactory {

  private ValidationHandlerFactory() {
  }

  private static final List<HttpMethod> validHttpMethods = List.of(HttpMethod.POST, HttpMethod.PUT);

  public static Handler<RoutingContext> create(Validator validator) {
    return routingContext -> {

      HttpMethod httpMethod = routingContext.request().method();
      if (!validHttpMethods.contains(httpMethod)) {
        routingContext.next();
        return;
      }

      OutputUnit validationResult = validator.validate(routingContext.body().asJsonObject());
      if (validationResult.getValid()) {
        routingContext.next();
      } else {
        JsonArray errors = new JsonArray();
        if (Objects.nonNull(validationResult.getErrors())) {
          for (OutputUnit error : validationResult.getErrors()) {
            log.warn(error.toString());
            errors.add(error.toJson());
          }
        }
        routingContext.fail(400, new ValidationFailedException(errors));
      }
    };
  }
}

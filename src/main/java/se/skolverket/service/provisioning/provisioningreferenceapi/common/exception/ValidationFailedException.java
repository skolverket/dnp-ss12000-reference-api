package se.skolverket.service.provisioning.provisioningreferenceapi.common.exception;

import io.vertx.core.json.JsonArray;

public class ValidationFailedException extends Exception{
  private JsonArray validationErrors;

  public ValidationFailedException(JsonArray validationErrors) {
    this.validationErrors = validationErrors;
  }

  public ValidationFailedException(String message, JsonArray validationErrors) {
    super(message);
    this.validationErrors = validationErrors;
  }

  public JsonArray getValidationErrors() {
    return validationErrors;
  }

  public void setValidationErrors(JsonArray validationErrors) {
    this.validationErrors = validationErrors;
  }
}

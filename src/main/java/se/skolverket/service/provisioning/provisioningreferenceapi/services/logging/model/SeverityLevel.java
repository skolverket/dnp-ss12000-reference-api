package se.skolverket.service.provisioning.provisioningreferenceapi.services.logging.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SeverityLevel {
  INFO("Info"),
  WARNING("Warning"),
  ERROR("Error");

  private final String value;

  SeverityLevel(String value) {
    this.value = value;
  }

  @JsonValue
  @Override
  public String toString() { return this.value; }
}

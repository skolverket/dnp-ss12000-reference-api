package se.skolverket.service.provisioning.provisioningreferenceapi.common.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ResourceType {
  GROUP("Group"),
  ACTIVITY("Activity"),
  DUTY("Duty"),
  PERSON("Person"),
  ORGANISATION("Organisation");

  private final String value;

  ResourceType(String value) { this.value = value; }

  @JsonValue
  @Override
  public String toString() { return this.value; }
}

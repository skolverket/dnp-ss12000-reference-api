package se.skolverket.service.provisioning.provisioningreferenceapi.common.model;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;


public enum ResourceType {
  GROUP("Group"),
  ACTIVITY("Activity"),
  DUTY("Duty"),
  PERSON("Person"),
  ORGANISATION("Organisation");

  private final String value;

  ResourceType(String value) { this.value = value; }


  private static final Map<String, ResourceType> BY_VALUE = new HashMap<>();

  static {
    for (ResourceType e : values()) {
      BY_VALUE.put(e.value, e);
    }
  }

  public static ResourceType fromValue(String label) {
    return BY_VALUE.get(label);
  }

  @JsonValue
  @Override
  public String toString() { return this.value; }
}

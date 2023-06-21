package se.skolverket.service.provisioning.provisioningreferenceapi.common.model;

import com.fasterxml.jackson.annotation.JsonValue;

@SuppressWarnings("SpellCheckingInspection")
public enum GroupType {
  UNDERVISNING("Undervisning"),
  KLASS("Klass"),
  MENTOR("Mentor"),
  PROVGRUPP("Provgrupp"),
  SCHEMA("Schema"),
  AVDELNING("Avdelning"),
  PERSONALGRUPP("Personalgrupp"),
  OVRIGT("Övrigt");

  private final String value;


  GroupType(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  public static GroupType fromString(String text) {
    for (GroupType g : GroupType.values()) {
      if (g.value.equalsIgnoreCase(text)) {
        return g;
      }
    }
    return null;
  }
  @Override
  public String toString() {
    return value;
  }
}

package se.skolverket.service.provisioning.provisioningreferenceapi.common.model;

import java.io.Serializable;

public class PersonReference extends ObjectReference implements Serializable {
  private static final long serialVersionUID = 1L;

  public PersonReference(String id, String displayName) {
    super(id, displayName);
  }

  public PersonReference() {
  }
}

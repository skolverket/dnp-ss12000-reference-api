package se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.*;
import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@DataObject
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Person extends DataType {

  @JsonProperty("givenName")
  private String givenName;

  @JsonProperty("middleName")
  private String middleName;

  @JsonProperty("familyName")
  private String familyName;

  @JsonProperty("eduPersonPrincipalNames")
  private List<String> eduPersonPrincipalNames;

  @JsonProperty("civicNo")
  private PersonCivicNo civicNo;

  @JsonProperty("emails")
  private List<Email> emails;

  @JsonProperty("enrolments")
  private List<Enrolment> enrolments;

  @JsonProperty("externalIdentifiers")
  private List<ExternalIdentifier> externalIdentifiers;


  public static Person fromBson(JsonObject bson) {
    return new Person(DataType.fromBsonJson(bson));
  }

  public Person(JsonObject jsonObject) {
    super(jsonObject);
    this.givenName = jsonObject.getString("givenName");
    this.middleName = jsonObject.getString("middleName");
    this.familyName = jsonObject.getString("familyName");
    this.eduPersonPrincipalNames = jsonObject.getJsonArray("eduPersonPrincipalNames") != null ?
      jsonObject.getJsonArray("eduPersonPrincipalNames")
        .stream().map(Object::toString)
        .collect(Collectors.toList()) : null;

    this.civicNo = jsonObject.getJsonObject("civicNo") != null ?
      jsonObject.getJsonObject("civicNo").mapTo(PersonCivicNo.class) : null;

    this.emails = Objects.requireNonNullElse(jsonObject.getJsonArray("emails", new JsonArray()), new JsonArray())
      .stream().map(o -> ((JsonObject)o).mapTo(Email.class))
        .collect(Collectors.toList());

    this.enrolments = Objects.requireNonNullElse(jsonObject.getJsonArray("enrolments", new JsonArray()), new JsonArray())
      .stream().map(o -> ((JsonObject)o).mapTo(Enrolment.class))
      .collect(Collectors.toList());

    this.externalIdentifiers = Objects.requireNonNullElse(jsonObject.getJsonArray("externalIdentifiers", new JsonArray()), new JsonArray())
      .stream().map(o -> ((JsonObject)o).mapTo(ExternalIdentifier.class))
      .collect(Collectors.toList());
  }
}

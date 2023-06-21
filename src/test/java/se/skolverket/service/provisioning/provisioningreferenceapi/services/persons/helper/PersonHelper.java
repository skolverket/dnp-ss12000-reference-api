package se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.helper;

import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.*;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.model.Person;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PersonHelper {

  private PersonHelper() {}

  public static Person validPerson() {
    var person = new Person();
    person.setId(UUID.randomUUID().toString());

    List<Enrolment> enrolments = List.of(new Enrolment(
      new SchoolUnitReference(UUID.randomUUID().toString(), null), 5, "School Type",
      LocalDate.now(),
      LocalDate.now().plusYears(1),
      false, "EDU123456789",
      new ObjectReference(UUID.randomUUID().toString(), "programme_name"),
      "Additional information regarding the education")
    );
    person.setEnrolments(enrolments);

    var civicNo = new PersonCivicNo();
    civicNo.setNationality("SWE");
    civicNo.setValue("121212121212");
    person.setCivicNo(civicNo);

    var eppns = new ArrayList<String>();
    eppns.add("testeppn@test.skolverket.se");
    person.setEduPersonPrincipalNames(eppns);

    var emails = new ArrayList<Email>();
    var email = new Email();
    email.setValue("testmail@skolverket.se");
    //email.setValue("TESTMAIL@skolverket.se");
    emails.add(email);
    person.setEmails(emails);

    person.setGivenName("Givenname");
    person.setMiddleName(null);
    person.setFamilyName("Familyname");
    return person;
  }

  public static Person validPersonWithEppn(String eppn) {
    Person person = validPerson();
    person.setEduPersonPrincipalNames(List.of(eppn));
    return person;
  }

  public static Person validPersonWithIdAndEppn(String id, String eppn) {
    Person person = validPersonWithEppn(eppn);
    person.setId(id);
    return person;
  }
}

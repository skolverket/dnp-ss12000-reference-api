package se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PersonTest {

  @Test
  void testJsonToPersonParsing() throws Exception {
    ObjectMapper mapper = DatabindCodec.mapper();
    mapper.registerModule(new JavaTimeModule());

    String file = "src/test/resources/sampledata/person.json";
    String json = readFileToString(file);
    JsonObject personJson = new JsonObject(json);

    Person person = new Person(personJson);
    assertEquals("123456789", person.getId());
    assertEquals("Given Name Test", person.getGivenName());
    assertEquals("Middle Name Test", person.getMiddleName());
    assertEquals("Family Name Test", person.getFamilyName());
    assertEquals("elev1@soderhavetsgymnasium.se", person.getEduPersonPrincipalNames().get(0));
    assertEquals("200112240129", person.getCivicNo().getValue());
    assertEquals("SWE", person.getCivicNo().getNationality());
    assertEquals("elev1@soderhavetsgymnasium.se", person.getEmails().get(0).getValue());
    assertEquals("Skola elev", person.getEmails().get(0).getType());
    assertEquals("fa592db1-912b-57fe-8cf6-df5f81522f01", person.getEnrolments().get(0).getEnroledAt().getId());
    assertEquals(1, person.getEnrolments().get(0).getSchoolYear());
    assertEquals("GY", person.getEnrolments().get(0).getSchoolType());
    assertEquals(false, person.getEnrolments().get(0).getCancelled());
    assertEquals("TEST_ED_1", person.getEnrolments().get(0).getEducationCode());
  }

   private static String readFileToString(String file)throws Exception {
     return new String(Files.readAllBytes(Paths.get(file)));
   }
}

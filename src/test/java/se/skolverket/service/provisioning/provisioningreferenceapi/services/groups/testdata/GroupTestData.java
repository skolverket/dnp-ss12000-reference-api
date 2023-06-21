package se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.testdata;

import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.*;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.model.Group;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static se.skolverket.service.provisioning.provisioningreferenceapi.common.model.GroupType.UNDERVISNING;

public final class GroupTestData {
  private GroupTestData() {
  }

  public static Group createInvalidGroup() {
    Group group = createValidGroup();
    group.setId("");
    group.setGroupType(null);

    return group;
  }

  public static Group createValidGroup() {
    Group group = new Group();
    group.setId(UUID.randomUUID().toString());
    group.setDisplayName("GroupName");
    group.setSchoolType(SchoolType.FS);
    group.setStartDate(LocalDate.now());
    group.setEndDate(LocalDate.now().plusDays(1));
    OrganisationReference orgRef = new OrganisationReference(UUID.randomUUID().toString(), "orgRefTest");
    group.setOrganisationReference(orgRef);
    group.setGroupType(UNDERVISNING);

    GroupMembership groupMembership = new GroupMembership();
    groupMembership.setId(UUID.randomUUID().toString());
    PersonReference personReference = new PersonReference(UUID.randomUUID().toString(), "personRefNameTest");
    groupMembership.setPerson(personReference);
    groupMembership.setStartDate(LocalDate.now());
    groupMembership.setEndDate((LocalDate.now().plusDays(1)));
    group.setGroupMemberships(List.of(groupMembership));

    return group;
  }
}

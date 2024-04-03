package se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.testdata;

import se.skolverket.service.provisioning.provisioningreferenceapi.common.model.*;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.model.Group;

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
    OrganisationReference orgRef = new OrganisationReference(UUID.randomUUID().toString(), "orgRefTest");
    group.setOrganisationReference(orgRef);
    group.setGroupType(UNDERVISNING);

    GroupMembership groupMembership = new GroupMembership();
    PersonReference personReference = new PersonReference(UUID.randomUUID().toString(), "personRefNameTest");
    groupMembership.setPerson(personReference);
    group.setGroupMemberships(List.of(groupMembership));

    return group;
  }
}

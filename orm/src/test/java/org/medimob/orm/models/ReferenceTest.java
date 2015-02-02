package org.medimob.orm.models;

import org.medimob.orm.annotation.Id;
import org.medimob.orm.annotation.Model;
import org.medimob.orm.annotation.Property;
import org.medimob.orm.annotation.Reference;

@Model
public class ReferenceTest {

  @Id
  long id;

  @Property
  @Reference(model = AllTypeTest.class)
  long simpleReferenceId;

  @Property
  @Reference(model = AllTypeTest.class)
  long cascadeDeleteId;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getSimpleReferenceId() {
    return simpleReferenceId;
  }

  public void setSimpleReferenceId(long simpleReferenceId) {
    this.simpleReferenceId = simpleReferenceId;
  }

  public long getCascadeDeleteId() {
    return cascadeDeleteId;
  }

  public void setCascadeDeleteId(long cascadeDeleteId) {
    this.cascadeDeleteId = cascadeDeleteId;
  }
}

package org.medimob.orm.models;

import org.medimob.orm.annotation.Id;
import org.medimob.orm.annotation.Model;
import org.medimob.orm.annotation.Property;

@Model
public class ColumnConstraintTest {

  @Id
  long id;

  @Property(name = "int_prop", check = "int_prop > 3")
  int greaterThan3;

  @Property(notNull = true)
  String notNullString;

  @Property(unique = true)
  String uniqueString;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public int getGreaterThan3() {
    return greaterThan3;
  }

  public void setGreaterThan3(int greaterThan3) {
    this.greaterThan3 = greaterThan3;
  }

  public String getNotNullString() {
    return notNullString;
  }

  public void setNotNullString(String notNullString) {
    this.notNullString = notNullString;
  }

  public String getUniqueString() {
    return uniqueString;
  }

  public void setUniqueString(String uniqueString) {
    this.uniqueString = uniqueString;
  }
}

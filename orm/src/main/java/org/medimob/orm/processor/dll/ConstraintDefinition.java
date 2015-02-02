package org.medimob.orm.processor.dll;

/**
 * Column or Table constraint definition. Created by Poopaou on 21/01/2015.
 */
public final class ConstraintDefinition {

  private final Constraints type;
  private final String name;
  private final String statement;

  ConstraintDefinition(Constraints type, String name, String statement) {
    this.type = type;
    this.name = name;
    this.statement = statement;
  }

  public Constraints getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public String getStatement() {
    return statement;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ConstraintDefinition that = (ConstraintDefinition) o;

    return !(name != null ? !name.equals(that.name) : that.name != null) && type == that.type;
  }

  @Override
  public int hashCode() {
    int result = type.hashCode();
    result = 31 * result + (name != null ? name.hashCode() : 0);
    return result;
  }
}

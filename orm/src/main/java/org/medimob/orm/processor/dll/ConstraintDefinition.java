package org.medimob.orm.processor.dll;

import javax.annotation.Nonnull;

/**
 * Created by Poopaou on 21/01/2015.
 */
public final class ConstraintDefinition {

  private final Constraints type;
  private final String name;
  private final String statement;

  ConstraintDefinition(@Nonnull Constraints type, @Nonnull String name, @Nonnull String statement) {
    this.type = type;
    this.name = name;
    this.statement = statement;
  }

  @Nonnull
  public Constraints getType() {
    return type;
  }

  @Nonnull
  public String getName() {
    return name;
  }

  @Nonnull
  public String getStatement() {
    return statement;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    ConstraintDefinition that = (ConstraintDefinition) obj;

    if (!name.equalsIgnoreCase(that.name)) {
      return false;
    }
    if (type != that.type) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = type.hashCode();
    result = 31 * result + name.hashCode();
    return result;
  }
}

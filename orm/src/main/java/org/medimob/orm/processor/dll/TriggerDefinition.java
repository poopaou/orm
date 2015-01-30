package org.medimob.orm.processor.dll;


/**
 * Trigger definition.
 *
 * Created by Poopaou on 17/01/2015.
 */
public final class TriggerDefinition {

  private final String name;
  private final String tableName;
  private final String statement;

  TriggerDefinition(String name, String tableName, String statement) {
    this.name = name;
    this.tableName = tableName;
    this.statement = statement;
  }

  public String getName() {
    return name;
  }

  public String getTableName() {
    return tableName;
  }

  public String getStatement() {
    return statement;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }

    TriggerDefinition that = (TriggerDefinition) other;

    return name.equalsIgnoreCase(that.name) && tableName.equalsIgnoreCase(that.tableName);

  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + tableName.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "TriggerModel{" + "name='" + name + '\'' + ", tableName=" + tableName + ", statement="
           + statement + '}';
  }
}

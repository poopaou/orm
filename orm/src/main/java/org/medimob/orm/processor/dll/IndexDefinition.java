package org.medimob.orm.processor.dll;


/**
 * Index definition.
 *
 * Created by Poopaou on 17/01/2015.
 */
public final class IndexDefinition {

  private final String name;
  private final String tableName;
  private final String statement;

  IndexDefinition(String name, String tableName, String statement) {
    this.name = name;
    this.tableName = tableName;
    this.statement = statement;
  }

  public String getTableName() {
    return tableName;
  }

  public String getName() {
    return name;
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

    IndexDefinition that = (IndexDefinition) other;

    return name.equalsIgnoreCase(that.name) && tableName.equalsIgnoreCase(that.tableName);
  }

  @Override
  public int hashCode() {
    int result = tableName.hashCode();
    result = 31 * result + name.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "IndexModel{" + "tableName=" + tableName + ", name='" + name + '\'' + ", statement="
           + statement + '}';
  }
}

package org.medimob.orm.processor.dll;

import org.medimob.orm.processor.PropertyType;


/**
 * Property definition.
 * Created by Poopaou on 17/01/2015.
 */
public final class PropertyDefinition {

  private final String fieldName;
  private final String columnName;
  private final PropertyType propertyType;
  private final String dateFormat;
  private final String statement;
  private final boolean insertable;
  private final boolean updateable;

  PropertyDefinition(String columnName, String fieldName,
                     PropertyType propertyType,
                     String dateFormat, String statement, boolean insertable,
                     boolean updateable) {
    this.columnName = columnName;
    this.fieldName = fieldName;
    this.propertyType = propertyType;
    this.dateFormat = dateFormat;
    this.statement = statement;
    this.insertable = insertable;
    this.updateable = updateable;
  }

  public String getFieldName() {
    return fieldName;
  }

  public String getColumnName() {
    return columnName;
  }

  public PropertyType getPropertyType() {
    return propertyType;
  }

  public String getStatement() {
    return statement;
  }

  public String getDateFormat() {
    return dateFormat;
  }

  public boolean isInsertable() {
    return insertable;
  }

  public boolean isUpdateable() {
    return updateable;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }

    PropertyDefinition that = (PropertyDefinition) other;

    return columnName.equalsIgnoreCase(that.columnName);

  }

  @Override
  public int hashCode() {
    return columnName.hashCode();
  }

  @Override
  public String toString() {
    return "ColumnModel{" + "columnName='" + columnName + '\'' + ", type=" + propertyType
           + ", statement=" + statement + '}';
  }
}

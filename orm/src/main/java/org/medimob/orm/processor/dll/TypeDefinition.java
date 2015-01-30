package org.medimob.orm.processor.dll;

/**
 * Type definition.
 *
 * Created by Poopaou on 17/01/2015.
 */
public final class TypeDefinition {

  private final String tableName;
  private final String typeSimpleName;
  private final String typeQualifiedName;
  private final String packageName;

  private final String beforeInsertMethod;
  private final String beforeUpdateMethod;
  private final String beforeDeleteMethod;
  // Properties
  private final PropertyDefinition idColumn;
  private final PropertyDefinition versionColumn;
  private final PropertyDefinition[] properties;
  private final ConstraintDefinition[] constraintDefinitions;
  private final TriggerDefinition[] triggers;
  private final IndexDefinition[] indexes;

  private final String statement;

  TypeDefinition(String tableName, String typeSimpleName,
                 String typeQualifiedName,
                 String packageName, String beforeInsertMethod,
                 String beforeUpdateMethod,
                 String beforeDeleteMethod, PropertyDefinition idColumn,
                 PropertyDefinition versionColumn, String statement,
                 PropertyDefinition[] properties,
                 ConstraintDefinition[] constraintDefinitions,
                 TriggerDefinition[] triggers, IndexDefinition[] indexes) {

    this.tableName = tableName;
    this.typeSimpleName = typeSimpleName;
    this.typeQualifiedName = typeQualifiedName;
    this.packageName = packageName;
    this.beforeInsertMethod = beforeInsertMethod;
    this.beforeUpdateMethod = beforeUpdateMethod;
    this.beforeDeleteMethod = beforeDeleteMethod;
    this.statement = statement;
    this.idColumn = idColumn;
    this.versionColumn = versionColumn;
    this.properties = properties;
    this.constraintDefinitions = constraintDefinitions;
    this.triggers = triggers;
    this.indexes = indexes;
  }

  public String getStatement() {
    return statement;
  }

  public String getTableName() {
    return tableName;
  }

  public String getTypeSimpleName() {
    return typeSimpleName;
  }

  public PropertyDefinition[] getProperties() {
    return properties;
  }

  public ConstraintDefinition[] getConstraintDefinitions() {
    return constraintDefinitions;
  }

  public String getTypeQualifiedName() {
    return typeQualifiedName;
  }

  public String getPackageName() {
    return packageName;
  }

  public String getBeforeInsertMethod() {
    return beforeInsertMethod;
  }

  public String getBeforeUpdateMethod() {
    return beforeUpdateMethod;
  }

  public String getBeforeDeleteMethod() {
    return beforeDeleteMethod;
  }

  public PropertyDefinition getIdColumn() {
    return idColumn;
  }

  public PropertyDefinition getVersionColumn() {
    return versionColumn;
  }

  public TriggerDefinition[] getTriggers() {
    return triggers;
  }

  public IndexDefinition[] getIndexes() {
    return indexes;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }

    TypeDefinition that = (TypeDefinition) other;

    return tableName.equalsIgnoreCase(that.tableName);
  }

  @Override
  public int hashCode() {
    return tableName.hashCode();
  }

  @Override
  public String toString() {
    return "TypeMeta{" + "tableName='" + tableName + '\'' + ", typeSimpleName='" + typeSimpleName
           + '\'' + ", typeQualifiedName='" + typeQualifiedName + '\'' + ", packageName='"
           + packageName + '\'' + ", beforeInsertMethod='" + beforeInsertMethod + '\''
           + ", beforeUpdateMethod='" + beforeUpdateMethod + '\'' + ", beforeDeleteMethod='"
           + beforeDeleteMethod + '\'' + ", idColumn=" + idColumn + ", statement='" + statement
           + '\'' + '}';
  }
}

package org.medimob.orm.processor.dll;

import android.support.annotation.Nullable;

import javax.annotation.Nonnull;

/**
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
  private final PropertyDefinition[] columns;
  private final ConstraintDefinition[] constraintDefinitions;
  private final TriggerDefinition[] triggers;
  private final IndexDefinition[] indexes;

  private final String statement;

  TypeDefinition(@Nonnull String tableName, @Nonnull String typeSimpleName,
                 @Nonnull String typeQualifiedName,
                 @Nonnull String packageName, @Nullable String beforeInsertMethod,
                 @Nullable String beforeUpdateMethod,
                 @Nullable String beforeDeleteMethod, @Nonnull PropertyDefinition idColumn,
                 @Nullable PropertyDefinition versionColumn, @Nonnull String statement,
                 @Nonnull PropertyDefinition[] columns,
                 @Nonnull ConstraintDefinition[] constraintDefinitions,
                 @Nonnull TriggerDefinition[] triggers, IndexDefinition[] indexes) {

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
    this.columns = columns;
    this.constraintDefinitions = constraintDefinitions;
    this.triggers = triggers;
    this.indexes = indexes;
  }

  @Nonnull
  public String getStatement() {
    return statement;
  }

  @Nonnull
  public String getTableName() {
    return tableName;
  }

  @Nonnull
  public String getTypeSimpleName() {
    return typeSimpleName;
  }

  @Nonnull
  public PropertyDefinition[] getColumns() {
    return columns;
  }

  @Nonnull
  public ConstraintDefinition[] getConstraintDefinitions() {
    return constraintDefinitions;
  }

  @Nonnull
  public String getTypeQualifiedName() {
    return typeQualifiedName;
  }

  @Nonnull
  public String getPackageName() {
    return packageName;
  }

  @Nullable
  public String getBeforeInsertMethod() {
    return beforeInsertMethod;
  }

  @Nullable
  public String getBeforeUpdateMethod() {
    return beforeUpdateMethod;
  }

  @Nullable
  public String getBeforeDeleteMethod() {
    return beforeDeleteMethod;
  }

  @Nonnull
  public PropertyDefinition getIdColumn() {
    return idColumn;
  }

  @Nullable
  public PropertyDefinition getVersionColumn() {
    return versionColumn;
  }

  @Nonnull
  public TriggerDefinition[] getTriggers() {
    return triggers;
  }

  @Nonnull
  public IndexDefinition[] getIndexes() {
    return indexes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TypeDefinition that = (TypeDefinition) o;

    if (!tableName.equalsIgnoreCase(that.tableName)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return tableName.hashCode();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("TypeMeta{");
    sb.append("tableName='").append(tableName).append('\'');
    sb.append(", typeSimpleName='").append(typeSimpleName).append('\'');
    sb.append(", typeQualifiedName='").append(typeQualifiedName).append('\'');
    sb.append(", packageName='").append(packageName).append('\'');
    sb.append(", beforeInsertMethod='").append(beforeInsertMethod).append('\'');
    sb.append(", beforeUpdateMethod='").append(beforeUpdateMethod).append('\'');
    sb.append(", beforeDeleteMethod='").append(beforeDeleteMethod).append('\'');
    sb.append(", idColumn=").append(idColumn);
    sb.append(", statement='").append(statement).append('\'');
    sb.append('}');
    return sb.toString();
  }
}

package org.medimob.orm.processor.dll;

import org.medimob.orm.internal.StatementBuilder;
import org.medimob.orm.processor.MappingException;

import java.util.HashSet;
import java.util.Set;

import static org.medimob.orm.processor.dll.DefinitionUtils.addOrThrowIfExist;
import static org.medimob.orm.processor.dll.DefinitionUtils.notEmpty;
import static org.medimob.orm.processor.dll.DefinitionUtils.notNull;

public class TypeDefinitionBuilder {

  private String tableName;
  private String typeSimpleName;
  private String typeQualifiedName;
  private String packageName;
  private String beforeInsertMethod;
  private String beforeUpdateMethod;
  private String beforeDeleteMethod;
  private boolean temporary;
  private String asStatement;
  private PropertyDefinition idColumn;
  private PropertyDefinition versionColumn;
  private Set<PropertyDefinition> columns = new HashSet<PropertyDefinition>();
  private Set<ConstraintDefinition> constraintDefinitions = new HashSet<ConstraintDefinition>();
  private Set<TriggerDefinition> triggers = new HashSet<TriggerDefinition>();
  private Set<IndexDefinition> indexes = new HashSet<IndexDefinition>();

  public TypeDefinitionBuilder setTableName(String tableName) {
    this.tableName = tableName;
    return this;
  }

  public TypeDefinitionBuilder setTypeSimpleName(String typeSimpleName) {
    this.typeSimpleName = typeSimpleName;
    return this;
  }

  public TypeDefinitionBuilder setTypeQualifiedName(String typeQualifiedName) {
    this.typeQualifiedName = typeQualifiedName;
    return this;
  }

  public TypeDefinitionBuilder setPackageName(String packageName) {
    this.packageName = packageName;
    return this;
  }

  public TypeDefinitionBuilder setBeforeInsertMethod(String beforeInsertMethod) {
    this.beforeInsertMethod = beforeInsertMethod;
    return this;
  }

  public TypeDefinitionBuilder setBeforeUpdateMethod(String beforeUpdateMethod) {
    this.beforeUpdateMethod = beforeUpdateMethod;
    return this;
  }

  public TypeDefinitionBuilder setBeforeDeleteMethod(String beforeDeleteMethod) {
    this.beforeDeleteMethod = beforeDeleteMethod;
    return this;
  }

  public TypeDefinitionBuilder setTemporary(boolean temporary) {
    this.temporary = temporary;
    return this;
  }

  public TypeDefinitionBuilder setAsStatement(String asStatement) {
    this.asStatement = asStatement;
    return this;
  }

  public TypeDefinitionBuilder setIdColumn(PropertyDefinition idColumn) {
    this.idColumn = idColumn;
    return this;
  }

  public TypeDefinitionBuilder setVersionColumn(PropertyDefinition versionColumn) {
    this.versionColumn = versionColumn;
    return this;
  }

  /**
   * Add a new column to type.
   *
   * @param column type column
   * @return this.
   * @throws MappingException if column is already defined.
   */
  public TypeDefinitionBuilder addColumn(PropertyDefinition column) throws MappingException {
    addOrThrowIfExist(columns, column, "Column " + column + " already exist in table");
    this.columns.add(column);
    return this;
  }

  /**
   * Add a new constraint to type.
   *
   * @param constraintDefinition constraint
   * @return this.
   * @throws MappingException if constraint is already defined.
   */
  public TypeDefinitionBuilder addConstraint(ConstraintDefinition constraintDefinition)
      throws MappingException {
    addOrThrowIfExist(constraintDefinitions, constraintDefinition,
                      "Constraint " + constraintDefinition + " already exist in table");
    return this;
  }

  /**
   * add a new trigger to type.
   *
   * @param trigger trigger
   * @return this.
   * @throws MappingException if trigger is already defined.
   */
  public TypeDefinitionBuilder addTrigger(TriggerDefinition trigger) throws MappingException {
    addOrThrowIfExist(triggers, trigger, "Trigger " + trigger + " already exist in table");
    return this;
  }

  /**
   * add a new index to type.
   *
   * @param index index
   * @return this.
   * @throws MappingException if index is already defined.
   */
  public TypeDefinitionBuilder addIndex(IndexDefinition index) throws MappingException {
    addOrThrowIfExist(indexes, index, "Index " + index + " already exist in table");
    return this;
  }

  /**
   * Build new type definition
   *
   * @return type definition.
   * @throws MappingException if definition is not valid.
   */
  public TypeDefinition build() throws MappingException {
    validate();
    return new TypeDefinition(tableName, typeSimpleName, typeQualifiedName, packageName,
                              beforeInsertMethod,
                              beforeUpdateMethod, beforeDeleteMethod, idColumn, versionColumn,
                              createStatement(),
                              columns.toArray(new PropertyDefinition[columns.size()]),
                              constraintDefinitions
                                  .toArray(new ConstraintDefinition[constraintDefinitions.size()]),
                              triggers.toArray(new TriggerDefinition[triggers.size()]),
                              indexes.toArray(new IndexDefinition[indexes.size()]));
  }

  private void validate() throws MappingException {
    notEmpty(typeQualifiedName, "Type's qualified name cannot be empty");
    notEmpty(typeSimpleName, "Type's simple name cannot be empty");
    notEmpty(packageName, "Type's package cannot be empty");
    notNull(idColumn, "Type's id column cannot be null");
  }

  private String createStatement() {
    StatementBuilder builder = new StatementBuilder();
    builder.appendWord("CREATE");
    if (temporary) {
      builder.appendWord("TEMPORARY");
    }
    builder.appendWord("TABLE");
    builder.appendWord(tableName.toUpperCase());
    if (asStatement != null && !asStatement.isEmpty()) {
      builder.appendWord("AS");
      builder.appendWord(asStatement);
    }
    return builder.toString();
  }
}
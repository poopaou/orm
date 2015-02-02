package org.medimob.orm.processor;

import org.medimob.orm.annotation.Conflict;
import org.medimob.orm.annotation.Id;
import org.medimob.orm.annotation.Index;
import org.medimob.orm.annotation.Model;
import org.medimob.orm.annotation.NotNull;
import org.medimob.orm.annotation.Property;
import org.medimob.orm.annotation.Reference;
import org.medimob.orm.annotation.Unique;
import org.medimob.orm.annotation.Version;
import org.medimob.orm.processor.dll.ConstraintDefinition;
import org.medimob.orm.processor.dll.Constraints;
import org.medimob.orm.processor.dll.IndexDefinition;
import org.medimob.orm.processor.dll.IndexDefinitionBuilder;
import org.medimob.orm.processor.dll.PropertyDefinition;
import org.medimob.orm.processor.dll.PropertyDefinitionBuilder;
import org.medimob.orm.processor.dll.TypeDefinition;
import org.medimob.orm.processor.dll.TypeDefinitionBuilder;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import static org.medimob.orm.processor.ProcessorUtils.resolveColumnName;
import static org.medimob.orm.processor.ProcessorUtils.typeToString;
import static org.medimob.orm.processor.dll.ConstraintDefinitionBuilder.newColumnConstraint;

/**
 * Property processor.
 *
 * Created by Poopaou on 18/01/2015.
 */
class PropertyProcessor {

  private EntityProcessor entityProcessor;
  private Types typeUtils;
  private Map<PropertyType, String> typeMirrorLookup;

  public PropertyProcessor(ProcessingEnvironment processingEnv, EntityProcessor entityProcessor) {
    this.typeUtils = processingEnv.getTypeUtils();
    this.entityProcessor = entityProcessor;
    initTypeLookup();
  }

  private static TypeMirror getClassTypeMirror(Reference annotation) {
    try {
      annotation.model(); // this should throw
    } catch (MirroredTypeException mte) {
      return mte.getTypeMirror();
    }
    return null; // can this ever happen ??
  }

  private void initTypeLookup() {
    // Create a lookup for types handled
    // by the SQLite database.
    Map<PropertyType, String> temp = new HashMap<PropertyType, String>();
    // Simple types.
    temp.put(PropertyType.BOOLEAN, Boolean.class.getCanonicalName());
    temp.put(PropertyType.CHARACTER, Character.class.getCanonicalName());
    temp.put(PropertyType.BYTE, Byte.class.getCanonicalName());
    temp.put(PropertyType.SHORT, Short.class.getCanonicalName());
    temp.put(PropertyType.INTEGER, Integer.class.getCanonicalName());
    temp.put(PropertyType.LONG, Long.class.getCanonicalName());
    temp.put(PropertyType.FLOAT, Float.class.getCanonicalName());
    temp.put(PropertyType.DOUBLE, Double.class.getCanonicalName());
    // Object type.
    temp.put(PropertyType.DATE_LONG, Date.class.getCanonicalName());
    temp.put(PropertyType.STRING, String.class.getCanonicalName());
    temp.put(PropertyType.BYTE_ARRAY, byte[].class.getCanonicalName());

    typeMirrorLookup = Collections.unmodifiableMap(temp);
  }

  public void process(VariableElement element,
                      TypeDefinitionBuilder typeDefinitionBuilder,
                      String tableName)
      throws MappingException {
    // If column annotation is present
    // on a @Id or @Version annotated
    // field the annotation will be
    // ignored.
    if (element.getAnnotation(Id.class) != null) {
      processId(element, typeDefinitionBuilder, element.getAnnotation(Id.class));
    } else if (element.getAnnotation(Version.class) != null) {
      processVersion(element, typeDefinitionBuilder, element.getAnnotation(Version.class));
    } else if (element.getAnnotation(Property.class) != null) {
      processProperty(element, typeDefinitionBuilder, tableName,
                      element.getAnnotation(Property.class));
    }
  }

  protected void processId(VariableElement element,
                           TypeDefinitionBuilder tableBuilder,
                           Id id) throws MappingException {
    // For simplification purpose @Id fields
    // are only supported for long type.
    if (PropertyType.LONG != getFieldType(element)) {
      throw new MappingException("@Id column must be of type long");
    }
    PropertyDefinition idPropertyDefinition = new PropertyDefinitionBuilder()
        .setColumnName(id.name())
        .setFieldName(element.getSimpleName().toString())
        .setPropertyType(getFieldType(element))
        .addConstraints(
            newColumnConstraint()
                .setType(Constraints.PRIMARY_KEY)
                .setName(id.name())
                .setAutoincrement(id.autoIncrement())
                .setConflictClause(id.onConflict())
                .setSort(id.sort())
                .build())
        .build();

    // Add column to the table model.
    tableBuilder.setIdColumn(idPropertyDefinition);
  }

  protected void processVersion(VariableElement element,
                                TypeDefinitionBuilder tableBuilder,
                                Version version) throws MappingException {
    PropertyType type = getFieldType(element);
    if (type == null || type != PropertyType.LONG && type != PropertyType.INTEGER) {
      throw new MappingException("@Version annotated column must be of type long or int");
    }
    // Version column.
    PropertyDefinition versionPropertyDefinition = new PropertyDefinitionBuilder()
        .setColumnName(version.name())
        .setFieldName(element.getSimpleName().toString())
        .setPropertyType(getFieldType(element))
        .addConstraints(newColumnConstraint()
                            .setType(Constraints.DEFAULT)
                            .setName(version.name())
                            .setExp("0")
                            .build())
        .addConstraints(newColumnConstraint()
                            .setType(Constraints.NOT_NULL)
                            .setName(version.name())
                            .setConflictClause(Conflict.ROLLBACK)
                            .build())
        .build();

    tableBuilder.setVersionColumn(versionPropertyDefinition);
  }

  protected void processProperty(VariableElement element,
                                 TypeDefinitionBuilder tableBuilder,
                                 String tableName, Property property) throws MappingException {
    // Resolve column name, by default if
    // no name is provided on the @Entity annotation
    // the column's name is equal to the property's
    // name.
    final String columnName = resolveColumnName(element, property);

    PropertyDefinitionBuilder columnBuilder = new PropertyDefinitionBuilder()
        .setColumnName(columnName)
        .setFieldName(element.getSimpleName().toString())
        .setInsertable(property.insertable())
        .setUpdateable(property.updatable());

    // Resolve type for basic types
    PropertyType type = getFieldType(element);
    if (type == null) {
      // Field type is not a 'basic' type :
      // Look for relationship like (oneToMany ManyToMany... etc).
      Element foreign = typeUtils.asElement(element.asType());
      if (foreign.getAnnotation(Model.class) == null) {
        throw new MappingException("");
      }
    } else {
      // Simple column mapping (boolean, String, integer...)
      // NB: Date field are by default mapped as 'long' it
      // can be modified with the @Column annotation
      // properties.
      columnBuilder.setPropertyType(getFieldType(element));
      if (type == PropertyType.DATE_STRING) {
        columnBuilder.setPropertyType(PropertyType.DATE_STRING);
        columnBuilder.setDateFormat(property.dateFormat());
      }
    }

    // Process columns constraints.
    processDefault(columnBuilder, property);
    processCollate(columnBuilder, property);
    processCheck(columnBuilder, property);
    processReference(columnBuilder, element);
    processUnique(columnBuilder, property, element.getAnnotation(Unique.class));
    processNotNull(columnBuilder, property, element.getAnnotation(NotNull.class));

    processIndexed(columnName, tableBuilder, tableName, property,
                   element.getAnnotation(Index.class));
    // Add column to the table model.
    tableBuilder.addColumn(columnBuilder.build());
  }

  private void processReference(PropertyDefinitionBuilder columnBuilder,
                                VariableElement element) throws MappingException {
    if (element.getAnnotation(Reference.class) == null) {
      return;
    }

    // Check property type (only long is supported).
    if (PropertyType.LONG != getFieldType(element)) {
      throw new MappingException("Reference property must be of type long");
    }

    Reference annotation = element.getAnnotation(Reference.class);
    TypeElement reference = (TypeElement) typeUtils.asElement(getClassTypeMirror(annotation));
    TypeDefinition typeDefinition = entityProcessor.getTypeDefinition(reference);

    ConstraintDefinition constraint = newColumnConstraint()
        .setType(Constraints.REFERENCES)
        .setReferenceTable(typeDefinition.getTableName())
        .setReferenceColumn(typeDefinition.getIdColumn().getColumnName())
        .setOnDeleteAction(annotation.onDelete())
        .setOnUpdateAction(annotation.onUpdate())
        .build();

    columnBuilder.addConstraints(constraint);
  }

  protected void processUnique(PropertyDefinitionBuilder builder, Property property,
                               Unique unique) throws MappingException {
    if (unique == null && property.unique()) {
      builder.addConstraints(newColumnConstraint()
                                 .setType(Constraints.UNIQUE)
                                 .setConflictClause(Conflict.ROLLBACK)
                                 .build());
    } else if (unique != null) {
      builder.addConstraints(newColumnConstraint()
                                 .setType(Constraints.UNIQUE)
                                 .setConflictClause(unique.onConflict())
                                 .build());
    }
  }

  protected void processNotNull(PropertyDefinitionBuilder builder, Property property,
                                NotNull notNull) throws MappingException {

    if (notNull == null && property.notNull()) {
      builder.addConstraints(newColumnConstraint()
                                 .setType(Constraints.NOT_NULL)
                                 .setConflictClause(Conflict.ROLLBACK)
                                 .build());
    } else if (notNull != null) {
      builder.addConstraints(newColumnConstraint()
                                 .setType(Constraints.NOT_NULL)
                                 .setConflictClause(notNull.onConflict())
                                 .build());
    }
  }

  protected void processIndexed(String columnName, TypeDefinitionBuilder tableBuilder,
                                String tableName, Property property,
                                Index index) throws MappingException {
    if (index == null && property.indexed()) {
      IndexDefinition indexDefinition = new IndexDefinitionBuilder()
          .setName(columnName)
          .setTableName(tableName)
          .setColumns(new String[]{columnName})
          .build();

      tableBuilder.addIndex(indexDefinition);
    } else if (index != null) {
      IndexDefinition indexDefinition = new IndexDefinitionBuilder()
          .setName(columnName)
          .setTableName(tableName)
          .setColumns(new String[]{columnName})
          .setWhere(index.where())
          .build();

      tableBuilder.addIndex(indexDefinition);
    }
  }

  protected void processCheck(PropertyDefinitionBuilder builder, Property property)
      throws MappingException {
    if (!"".equals(property.check())) {
      builder.addConstraints(newColumnConstraint()
                                 .setType(Constraints.CHECK)
                                 .setExp(property.check())
                                 .build());
    }
  }

  protected void processDefault(PropertyDefinitionBuilder builder, Property property)
      throws MappingException {
    if (!"".equals(property.defaultValue())) {
      builder.addConstraints(newColumnConstraint()
                                 .setType(Constraints.DEFAULT)
                                 .setExp(property.defaultValue())
                                 .build());
    }
  }

  protected void processCollate(PropertyDefinitionBuilder builder, Property property)
      throws MappingException {
    if (!"".equals(property.collate())) {
      builder.addConstraints(newColumnConstraint()
                                 .setType(Constraints.COLLATE)
                                 .setExp(property.collate())
                                 .build());
    }
  }

  private PropertyType getFieldType(VariableElement element) throws MappingException {
    TypeMirror type = element.asType();
    if (type instanceof ArrayType) {
      ArrayType arrayType = (ArrayType) type;
      TypeMirror componentType = arrayType.getComponentType();
      String typeName = typeToString(componentType);
      if (componentType instanceof PrimitiveType && typeMirrorLookup.get(PropertyType.BYTE)
          .equals(typeName)) {
        return PropertyType.BYTE_ARRAY;
      } else {
        throw new MappingException("Only array of byte are handled");
      }
    } else {
      String typeString = typeToString(element.asType());
      for (Map.Entry<PropertyType, String> entry : typeMirrorLookup.entrySet()) {
        if (typeString.equals(entry.getValue())) {
          return entry.getKey();
        }
      }
    }
    return null;
  }
}

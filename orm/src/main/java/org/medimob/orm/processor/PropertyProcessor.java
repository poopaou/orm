package org.medimob.orm.processor;

import org.medimob.orm.annotation.*;
import org.medimob.orm.processor.dll.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.medimob.orm.processor.ProcessorUtils.*;

/**
 * Created by Poopaou on 18/01/2015.
 */
class PropertyProcessor {

    private Types typeUtils;
    private Map<PropertyType, String> typeMirrorLookup;

    public PropertyProcessor(ProcessingEnvironment processingEnv) {
        this.typeUtils = processingEnv.getTypeUtils();
        initTypeLookup();
    }

    private void initTypeLookup(){
        // Create a lookup for types handled
        // by the SQLite database.
        Map<PropertyType, String> temp= new HashMap<PropertyType, String>();
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

    public void process(@Nonnull VariableElement element, @Nonnull TypeDefinitionBuilder typeDefinitionBuilder, @Nonnull String tableName)
            throws MappingException {
        // If column annotation is present
        // on a @Id or @Version annotated
        // field the annotation will be
        // ignored.
        if (element.getAnnotation(Id.class) != null){
            processId(element, typeDefinitionBuilder, element.getAnnotation(Id.class));
        }
        else if (element.getAnnotation(Version.class) != null){
            processVersion(element, typeDefinitionBuilder, element.getAnnotation(Version.class));
        }
        else if(element.getAnnotation(Column.class) != null) {
            processColumn(element, typeDefinitionBuilder, tableName, element.getAnnotation(Column.class));
        }
    }

    protected void processId(@Nonnull VariableElement element, @Nonnull TypeDefinitionBuilder tableBuilder,
                           @Nonnull Id id) throws MappingException {
        // For simplification purpose @Id fields
        // are only supported for long type.
        if (PropertyType.LONG != getFieldType(element)){
            throw new MappingException("@Id column must be of type long");
        }
        PropertyDefinition idPropertyDefinition = new PropertyDefinitionBuilder()
                .setColumnName(id.name())
                .setFieldName(element.getSimpleName().toString())
                .setPropertyType(getFieldType(element))
                .addConstraints(
                        ConstraintDefinitionBuilder.newColumnConstraint()
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

    protected void processVersion(@Nonnull VariableElement element, @Nonnull TypeDefinitionBuilder tableBuilder,
                                @Nonnull Version version) throws MappingException {
        PropertyType type = getFieldType(element);
        if (type == null || type != PropertyType.LONG && type != PropertyType.INTEGER) {
            throw new MappingException("@Version annotated column must be of type long or int");
        }
        // Version column.
        PropertyDefinition versionPropertyDefinition = new PropertyDefinitionBuilder()
                .setColumnName(version.name())
                .setFieldName(element.getSimpleName().toString())
                .setPropertyType(getFieldType(element))
                .addConstraints(ConstraintDefinitionBuilder.newColumnConstraint()
                        .setType(Constraints.DEFAULT)
                        .setName(version.name())
                        .setExp("0")
                        .build())
                .addConstraints(ConstraintDefinitionBuilder.newColumnConstraint()
                        .setType(Constraints.NOT_NULL)
                        .setName(version.name())
                        .setConflictClause(Conflict.ROLLBACK)
                        .build())
                .build();

        tableBuilder.setVersionColumn(versionPropertyDefinition);
    }

    protected void processColumn(@Nonnull VariableElement element, @Nonnull TypeDefinitionBuilder tableBuilder,
                                 String tableName, @Nonnull Column column) throws MappingException {
        // Resolve column name, by default if
        // no name is provided on the @Entity annotation
        // the column's name is equal to the property's
        // name.
        final String columnName = resolveColumnName(element, column);

        PropertyDefinitionBuilder columnBuilder = new PropertyDefinitionBuilder()
                .setColumnName(columnName)
                .setFieldName(element.getSimpleName().toString())
                .setInsertable(column.insertable())
                .setUpdateable(column.updatable());

        // Resolve type for basic types
        PropertyType type = getFieldType(element);
        if (type == null){
            // Field type is not a 'basic' type :
            // Look for relationship like (oneToMany ManyToMany... etc).
            Element foreign = typeUtils.asElement(element.asType());
            if (foreign.getAnnotation(Entity.class) == null){
                throw new MappingException("");
            }
        }
        else {
            // Simple column mapping (boolean, String, integer...)
            // NB: Date field are by default mapped as 'long' it
            // can be modified with the @Column annotation
            // properties.
            columnBuilder.setPropertyType(getFieldType(element));
            if(type == PropertyType.DATE_STRING){
                columnBuilder.setPropertyType(PropertyType.DATE_STRING);
                columnBuilder.setDateFormat(column.dateFormat());
            }
        }

        // Process columns constraints.
        processDefault(columnName, columnBuilder, column);
        processCollate(columnName, columnBuilder, column);
        processUnique(columnName, columnBuilder, column, element.getAnnotation(Unique.class));
        processNotNull(columnName, columnBuilder, column, element.getAnnotation(NotNull.class));
        processCheck(columnName, columnBuilder, column, element.getAnnotation(Check.class));
        processIndexed(columnName, tableBuilder, tableName, column, element.getAnnotation(Index.class));

        // Add column to the table model.
        tableBuilder.addColumn(columnBuilder.build());
    }

    protected void processUnique(@Nonnull String columnName, @Nonnull PropertyDefinitionBuilder builder, @Nonnull Column column,
                               @Nullable Unique unique) throws MappingException {
        if (unique == null && column.unique()){
            builder.addConstraints(ConstraintDefinitionBuilder.newColumnConstraint()
                    .setType(Constraints.UNIQUE)
                    .setName(columnName)
                    .setConflictClause(Conflict.ROLLBACK)
                    .build());
        }
        else if (unique != null){
            builder.addConstraints(ConstraintDefinitionBuilder.newColumnConstraint()
                    .setType(Constraints.UNIQUE)
                    .setName(resolveUniqueName(unique, columnName))
                    .setConflictClause(unique.onConflict())
                    .build());
        }
    }

    protected void processNotNull(@Nonnull String columnName, @Nonnull PropertyDefinitionBuilder builder,
                                  @Nonnull Column column, @Nullable NotNull notNull) throws MappingException {

        if(notNull == null && column.notNull()){
            builder.addConstraints(ConstraintDefinitionBuilder.newColumnConstraint()
                    .setType(Constraints.NOT_NULL)
                    .setName(columnName)
                    .setConflictClause(Conflict.ROLLBACK)
                    .build());
        }
        else if (notNull != null){
            builder.addConstraints(ConstraintDefinitionBuilder.newColumnConstraint()
                    .setType(Constraints.NOT_NULL)
                    .setName(columnName)
                    .setConflictClause(notNull.onConflict())
                    .build());
        }
    }

    protected void processIndexed(String columnName, @Nonnull TypeDefinitionBuilder tableBuilder, String tableName, @Nonnull Column column,
                                  @Nullable Index index) throws MappingException {
        if (index == null && column.indexed()){
            IndexDefinition indexDefinition = new IndexDefinitionBuilder()
                    .setName(columnName)
                    .setTableName(tableName)
                    .setColumns(new String[]{columnName})
                    .build();

            tableBuilder.addIndex(indexDefinition);
        }
        else if (index != null){
            IndexDefinition indexDefinition = new IndexDefinitionBuilder()
                    .setName(columnName)
                    .setTableName(tableName)
                    .setColumns(new String[]{columnName})
                    .setWhere(index.where())
                    .build();

            tableBuilder.addIndex(indexDefinition);
        }
    }

    protected void processCheck(String columnName, @Nonnull PropertyDefinitionBuilder builder, @Nonnull Column column,
                                @Nullable Check check) throws MappingException {
        if(check == null && !column.check().equals("")){
            builder.addConstraints(ConstraintDefinitionBuilder.newColumnConstraint()
                    .setType(Constraints.CHECK)
                    .setName(columnName)
                    .setExp(column.check())
                    .build());
        }
        else if (check != null){
            builder.addConstraints(ConstraintDefinitionBuilder.newColumnConstraint()
                    .setType(Constraints.CHECK)
                    .setName(columnName)
                    .setExp(check.exp())
                    .build());
        }
    }

    protected void processDefault(String columnName, PropertyDefinitionBuilder builder, Column column) throws MappingException {
        if (!column.defaultValue().equals("")){
            builder.addConstraints(ConstraintDefinitionBuilder.newColumnConstraint()
                    .setType(Constraints.DEFAULT)
                    .setName(columnName)
                    .setExp(column.defaultValue())
                    .build());
        }
    }

    protected void processCollate(String columnName, PropertyDefinitionBuilder builder, Column column) throws MappingException {
        if (!column.collate().equals("")){
            builder.addConstraints(ConstraintDefinitionBuilder.newColumnConstraint()
                    .setType(Constraints.COLLATE)
                    .setName(columnName)
                    .setExp(column.collate())
                    .build());
        }
    }

    private PropertyType getFieldType(VariableElement element) throws MappingException {
        TypeMirror type = element.asType();
        if (type instanceof ArrayType){
            ArrayType arrayType = (ArrayType) type;
            TypeMirror componentType = arrayType.getComponentType();
            String typeName = typeToString(componentType);
            if (componentType instanceof PrimitiveType &&typeMirrorLookup.get(PropertyType.BYTE).equals(typeName)){
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

package org.medimob.orm.processor.dll;

import org.medimob.orm.processor.MappingException;
import org.medimob.orm.processor.PropertyType;

import java.util.HashSet;

import static org.medimob.orm.processor.dll.DefinitionUtils.*;

public class PropertyDefinitionBuilder {

    private String fieldName;
    private String columnName;
    private PropertyType propertyType;
    private String dateFormat;
    private boolean insertable;
    private boolean updateable;
    private HashSet<ConstraintDefinition> constraintDefinitions = new HashSet<ConstraintDefinition>();

    public PropertyDefinitionBuilder setFieldName(String fieldName) {
        this.fieldName = fieldName;
        return this;
    }

    public PropertyDefinitionBuilder setColumnName(String columnName) {
        this.columnName = columnName;
        return this;
    }

    public PropertyDefinitionBuilder setPropertyType(PropertyType propertyType) {
        this.propertyType = propertyType;
        return this;
    }

    public PropertyDefinitionBuilder setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
        return this;
    }

    public PropertyDefinitionBuilder addConstraints(ConstraintDefinition constraintDefinition) throws MappingException {
        addOrThrowIfExist(constraintDefinitions, constraintDefinition, "Constraint " + constraintDefinition + " already exist for property");
        return this;
    }

    public PropertyDefinitionBuilder setInsertable(boolean insertable) {
        this.insertable = insertable;
        return this;
    }

    public PropertyDefinitionBuilder setUpdateable(boolean updateable) {
        this.updateable = updateable;
        return this;
    }

    public PropertyDefinition build() throws MappingException {
        validate();
        return new PropertyDefinition(columnName, fieldName, propertyType, dateFormat, getStatement(), insertable, updateable);
    }

    private void validate() throws MappingException {
        notEmpty(columnName, "Column's name is empty for property " + this);
        notEmpty(fieldName, "Column's field is empty for property " + this);
        notNull(propertyType, "Column's type is null  for property " + this);
        if (propertyType == PropertyType.DATE_STRING){
            notEmpty(dateFormat, "Date format is required for DATE_STRING format for property " + this);
        }
    }

    private String getStatement(){
        StatementBuilder builder = new StatementBuilder();
        builder.appendWord(columnName.toUpperCase());
        if (propertyType != null){
            builder.appendWord(propertyType.mappedType);
        }
        for (ConstraintDefinition constraintDefinitionDef : constraintDefinitions){
            builder.appendWord(constraintDefinitionDef.getStatement());
        }
        return builder.toString();
    }
}
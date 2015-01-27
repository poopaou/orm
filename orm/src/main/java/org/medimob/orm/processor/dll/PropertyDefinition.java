package org.medimob.orm.processor.dll;

import android.support.annotation.Nullable;
import org.medimob.orm.processor.PropertyType;

import javax.annotation.Nonnull;

/**
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

    PropertyDefinition(@Nonnull String columnName, @Nonnull String fieldName, @Nonnull PropertyType propertyType,
                       @Nullable String dateFormat, @Nonnull String statement, boolean insertable, boolean updateable) {
        this.columnName = columnName;
        this.fieldName = fieldName;
        this.propertyType = propertyType;
        this.dateFormat = dateFormat;
        this.statement = statement;
        this.insertable = insertable;
        this.updateable = updateable;
    }

    @Nonnull
    public String getFieldName() {
        return fieldName;
    }

    @Nonnull
    public String getColumnName() {
        return columnName;
    }

    @Nonnull
    public PropertyType getPropertyType() {
        return propertyType;
    }

    @Nonnull
    public String getStatement() {
        return statement;
    }

    @Nullable
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PropertyDefinition that = (PropertyDefinition) o;

        if (!columnName.equalsIgnoreCase(that.columnName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return columnName.hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ColumnModel{");
        sb.append("columnName='").append(columnName).append('\'');
        sb.append(", type=").append(propertyType);
        sb.append(", statement=").append(statement);
        sb.append('}');
        return sb.toString();
    }
}

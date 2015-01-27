package org.medimob.orm.processor.dll;

import javax.annotation.Nonnull;

/**
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

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public String getTableName() {
        return tableName;
    }

    @Nonnull
    public String getStatement() {
        return statement;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TriggerDefinition that = (TriggerDefinition) o;

        if (!name.equalsIgnoreCase(that.name)) return false;
        if (!tableName.equalsIgnoreCase(that.tableName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + tableName.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TriggerModel{");
        sb.append("name='").append(name).append('\'');
        sb.append(", tableName=").append(tableName);
        sb.append(", statement=").append(statement);
        sb.append('}');
        return sb.toString();
    }
}

package org.medimob.orm.processor.dll;

import org.medimob.orm.annotation.TriggerType;
import org.medimob.orm.processor.MappingException;

public class TriggerDefinitionBuilder {

    private String name;
    private String tableName;
    private boolean temporary;
    private TriggerType triggerType;
    private boolean forEachRow;
    private String when;
    private String[] statements;

    public TriggerDefinitionBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public TriggerDefinitionBuilder setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public TriggerDefinitionBuilder setTemporary(boolean temporary) {
        this.temporary = temporary;
        return this;
    }

    public TriggerDefinitionBuilder setTriggerType(TriggerType triggerType) {
        this.triggerType = triggerType;
        return this;
    }

    public TriggerDefinitionBuilder setForEachRow(boolean forEachRow) {
        this.forEachRow = forEachRow;
        return this;
    }

    public TriggerDefinitionBuilder setWhen(String when) {
        this.when = when;
        return this;
    }

    public TriggerDefinitionBuilder setStatements(String[] statements) {
        this.statements = statements;
        return this;
    }

    public TriggerDefinition build() throws MappingException {
        validate();
        return new TriggerDefinition(name, tableName, createStatement());
    }

    private void validate() throws MappingException {
        DefinitionUtils.notEmpty(name, "Trigger's name cannot be empty");
        DefinitionUtils.notNull(triggerType, "Trigger's type cannot be null");
        DefinitionUtils.notEmpty(tableName, "Trigger's table cannot be empty");
        DefinitionUtils.notNull(statements, "Trigger's statement(s) cannot be empty");
    }

    private String createStatement(){
        StatementBuilder builder = new StatementBuilder();
        builder.appendWord("CREATE");
        if (temporary){
            builder.appendWord("TEMPORARY");
        }
        builder.appendWord("TRIGGER");
        builder.appendWord("TRI_" + name.toUpperCase());
        if (triggerType != null){
            builder.appendWord(triggerType.getSql());
        }
        builder.appendWord("ON");
        builder.appendWord(tableName.toUpperCase());
        if (forEachRow){
            builder.appendWord("FOR");
            builder.appendWord("EACH");
            builder.appendWord("ROW");
        }
        if (when != null){
            builder.appendWord("WHERE");
            builder.appendBetweenBracket(when);
        }
        builder.appendWord("BEGIN");
        builder.appendWithSeparator(statements, ';');
        builder.appendWord("END");
        return builder.toString();
    }
}
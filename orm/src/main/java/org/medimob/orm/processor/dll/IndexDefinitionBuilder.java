package org.medimob.orm.processor.dll;

import org.medimob.orm.processor.MappingException;

public class IndexDefinitionBuilder {
    private String name;
    private String tableName;
    private boolean unique;
    private String[] columns;
    private String where;

    public IndexDefinitionBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public IndexDefinitionBuilder setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public IndexDefinitionBuilder setUnique(boolean unique) {
        this.unique = unique;
        return this;
    }

    public IndexDefinitionBuilder setColumns(String[] columns) {
        this.columns = columns;
        return this;
    }

    public IndexDefinitionBuilder setWhere(String where) {
        this.where = where;
        return this;
    }

    public IndexDefinition build() throws MappingException {
        validate();
        return new IndexDefinition(name, tableName, createStatement());
    }

    private void validate() throws MappingException{
        DefinitionUtils.notEmpty(name, "Index's name cannot be empty");
        DefinitionUtils.notEmpty(tableName, "Index's table cannot be empty");
        DefinitionUtils.notNull(columns, "Index's columns cannot be null");
    }

    private String createStatement(){
        StatementBuilder builder = new StatementBuilder();
        builder.appendWord("CREATE");
        if (unique){
            builder.appendWord("UNIQUE");
        }
        builder.appendWord("INDEX");
        builder.appendWord(name.toUpperCase());
        builder.appendWord("ON");
        builder.appendWord(tableName.toUpperCase());
        builder.openBracket();
        builder.appendWithSeparator(columns, ',');
        builder.closeBracket();
        if(where != null){
            builder.appendWord("WHERE");
            builder.appendWord(name);
        }
        return builder.toString();
    }
}
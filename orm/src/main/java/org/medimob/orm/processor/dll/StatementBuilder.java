package org.medimob.orm.processor.dll;

/**
 * Created by Poopaou on 17/01/2015.
 */
public class StatementBuilder {

    private StringBuilder builder;

    public StatementBuilder() {
        this.builder = new StringBuilder();
    }

    public StatementBuilder appendWord(String word){
        this.builder.append(' ')
                .append(word);
        return this;
    }

    public StatementBuilder appendBetweenBracket(String exp){
        this.builder.append(" (")
                .append(exp)
                .append(") ");
        return this;
    }

    public StatementBuilder openBracket(){
        this.builder.append(" ( ");
        return this;
    }

    public StatementBuilder appendWithSeparator(String[] strings, char separator){
        for (int i = 0; i < strings.length; i++){
            if (i > 0){
                builder.append(' ');
                builder.append(separator);
            }
            builder.append(strings[i]);
        }
        return this;
    }

    public StatementBuilder closeBracket(){
        this.builder.append(" )");
        return this;
    }

    @Override
    public String toString() {
        return builder.toString().trim();
    }
}

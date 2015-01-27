package org.medimob.orm.annotation;

/**
 * Created by Poopaou on 17/01/2015.
 */
public enum Conflict {
    ROLLBACK ("ON CONFLICT ROLLBACK"),
    ABORT ("ON CONFLICT ABORT"),
    FAIL ("ON CONFLICT FAIL"),
    IGNORE ("ON CONFLICT IGNORE"),
    REPLACE ("ON CONFLICT REPLACE");

    private final String sql;

    private Conflict(String sql){
        this.sql = sql;
    }

    public String getSql(){
        return sql;
    }
}

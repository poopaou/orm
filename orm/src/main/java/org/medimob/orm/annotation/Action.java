package org.medimob.orm.annotation;

/**
 * References onDelete or onUpdate actions. Created by Poopaou on 02/02/2015.
 */
public enum Action {
  NO_ACTION("NO ACTION"),
  RESTRICT("RESTRICT"),
  SET_NULL("SET NULL"),
  SET_DEFAULT("SET DEFAULT"),
  CASCADE("CASCADE");

  private final String sql;

  Action(String sql) {
    this.sql = sql;
  }

  public String getSql() {
    return sql;
  }
}

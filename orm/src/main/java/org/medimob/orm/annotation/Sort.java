package org.medimob.orm.annotation;

/**
 * Created by Poopaou on 17/01/2015.
 */
public enum Sort {
  ASC("ASC"),
  DESC("DESC"),
  NONE("");

  private final String sql;

  private Sort(String sql) {
    this.sql = sql;
  }

  public String getSql() {
    return sql;
  }
}

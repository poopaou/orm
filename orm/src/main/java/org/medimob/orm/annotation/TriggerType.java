package org.medimob.orm.annotation;

/**
 * Created by Poopaou on 17/01/2015.
 */
public enum TriggerType {
  // Basic triggers.
  INSERT("INSERT"),
  UPDATE("UPDATE"),
  DELETE("DELETE"),
  // Before triggers.
  BEFORE_INSERT("BEFORE INSERT"),
  BEFORE_UPDATE("BEFORE UPDATE"),
  BEFORE_DELETE("BEFORE DELETE"),
  // After triggers.
  AFTER_INSERT("AFTER INSERT"),
  AFTER_UPDATE("AFTER UPDATE"),
  AFTER_DELETE("AFTER DELETE"),
  // Instead of triggers.
  INSTEAD_OF_INSERT("INSTEAD OF INSERT"),
  INSTEAD_OF_UPDATE("INSTEAD OF UPDATE"),
  INSTEAD_OF_DELETE("INSTEAD OF DELETE");

  private final String sql;

  private TriggerType(String sql) {
    this.sql = sql;
  }

  public String getSql() {
    return sql;
  }
}

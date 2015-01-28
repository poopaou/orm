package org.medimob.orm.processor.dll;

/**
 * Created by Poopaou on 17/01/2015.
 */
public enum Constraints {
  UNIQUE("UNIQUE", true, true),
  CHECK("CHECK", true, true),
  PRIMARY_KEY("PRIMARY KEY", true, true),
  NOT_NULL("NOT NULL", false, true),
  DEFAULT("DEFAULT", false, true),
  COLLATE("COLLATE", false, true),
  FOREIGN_KEY("FOREIGN KEY", true, false);

  private final String sql;
  private final boolean forTable;
  private final boolean forColumn;

  Constraints(String sql, boolean forTable, boolean forColumn) {
    this.sql = sql;
    this.forTable = forTable;
    this.forColumn = forColumn;
  }

  public String getSql() {
    return sql;
  }

  public boolean isForTable() {
    return forTable;
  }

  public boolean isForColumn() {
    return forColumn;
  }
}

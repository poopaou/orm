package org.medimob.orm;

import java.util.List;

/**
 * Created by Poopaou on 30/01/2015.
 */
public class Query<T> {

  private final Property[] properties;
  private final String table;


  private String[] groupBy;
  private String[] orderBy;
  private boolean distinct;
  private String having;
  private List<String> wheres;

  public Query(Property[] properties, String table) {
    this.properties = properties;
    this.table = table;
  }

  public Query<T> distinct() {
    distinct = true;
    return this;
  }

  public Query<T> having(String exp) {
    having = exp;
    return this;
  }

  public T querySingle() {
    return null;
  }

  public T query() {
    return null;
  }

  public static final class Property {

    private final String name;
    private final String column;

    public Property(String name, String column) {
      this.name = name;
      this.column = column;
    }

    String getName() {
      return name;
    }

    String getColumn() {
      return column;
    }
  }

}

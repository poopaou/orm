package org.medimob.orm;

/**
 * Created by Poopaou on 30/01/2015.
 */
public final class Property {

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
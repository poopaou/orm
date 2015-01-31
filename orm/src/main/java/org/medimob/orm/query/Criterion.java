package org.medimob.orm.query;

import org.medimob.orm.internal.StatementBuilder;

import java.util.ArrayList;

/**
 * Created by Poopaou on 30/01/2015.
 */
public interface Criterion {

  public static final String PLACE_HOLDER = "?";

  void collectArgs(ArrayList<String> argsList);

  void toSql(StatementBuilder builder, PropertyResolver resolver);

  int getArgsCount();

}

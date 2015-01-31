package org.medimob.orm.query;

import org.medimob.orm.internal.StatementBuilder;

import java.util.ArrayList;

/**
 * Created by Poopaou on 30/01/2015.
 */
public class IdCriterion implements Criterion {

  private final long id;

  public IdCriterion(long id) {
    this.id = id;
  }

  @Override
  public void collectArgs(ArrayList<String> argsList) {

  }

  @Override
  public void toSql(StatementBuilder builder, PropertyResolver resolver) {
    builder.appendWord(resolver.resolveIdColumn());
    builder.appendWord("==");
    builder.appendWord(Long.toString(id));
  }

  @Override
  public int getArgsCount() {
    return 0;
  }
}

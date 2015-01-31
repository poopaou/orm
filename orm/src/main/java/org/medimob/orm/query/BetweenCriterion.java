package org.medimob.orm.query;

import org.medimob.orm.OrmException;
import org.medimob.orm.internal.StatementBuilder;

import java.util.ArrayList;

/**
 * Created by Poopaou on 30/01/2015.
 */
public class BetweenCriterion implements NotCriterion {

  private final Object between;
  private final Object and;
  private final String property;
  private boolean not;

  public BetweenCriterion(String property, Object between, Object and) {
    this.between = between;
    this.and = and;
    this.property = property;
  }

  @Override
  public void collectArgs(ArrayList<String> argsList) {
    if (between == null || and == null) {
      throw new OrmException("Missing between value");
    }
    argsList.add(between.toString());
    argsList.add(and.toString());
  }

  @Override
  public void toSql(StatementBuilder builder, PropertyResolver resolver) {
    builder.appendWord(resolver.resolveColumnForProperty(property));
    if (not) {
      builder.appendWord("NOT");
    }
    builder.appendWord("BETWEEN");
    builder.appendWord(between.toString());
    builder.appendWord("AND");
    builder.appendWord(and.toString());
  }

  @Override
  public int getArgsCount() {
    return 2;
  }

  @Override
  public Criterion not() {
    not = true;
    return this;
  }
}

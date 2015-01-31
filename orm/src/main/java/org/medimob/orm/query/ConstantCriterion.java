package org.medimob.orm.query;

import org.medimob.orm.internal.StatementBuilder;

import java.util.ArrayList;

/**
 * Created by Poopaou on 30/01/2015.
 */
public class ConstantCriterion implements Criterion {

  private final String constant;
  private final String property;

  public ConstantCriterion(String constant, String property) {
    this.constant = constant;
    this.property = property;
  }

  @Override
  public void collectArgs(ArrayList<String> argsList) {
    // nothing to do
  }

  @Override
  public void toSql(StatementBuilder builder, PropertyResolver resolver) {
    builder.appendWord(resolver.resolveColumnForProperty(property));
    builder.appendWord(constant);

  }

  @Override
  public int getArgsCount() {
    return 0;
  }
}

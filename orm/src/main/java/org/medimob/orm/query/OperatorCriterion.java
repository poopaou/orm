package org.medimob.orm.query;

import org.medimob.orm.OrmException;
import org.medimob.orm.internal.StatementBuilder;

import java.util.ArrayList;

/**
 * Created by Poopaou on 30/01/2015.
 */
public class OperatorCriterion implements NotCriterion {

  public final String operator;
  public final String property;
  public final Object value;
  private boolean not;

  public OperatorCriterion(String operator, String property, Object val) {
    this.operator = operator;
    this.property = property;
    this.value = val;
  }

  @Override
  public void collectArgs(ArrayList<String> argsList) {
    if (value == null) {
      throw new OrmException(
          String.format("Operator %s value is null for property %S", operator, property));
    }
    argsList.add(value.toString());
  }

  @Override
  public void toSql(StatementBuilder builder, PropertyResolver resolver) {
    builder.appendWord(resolver.resolveColumnForProperty(property));
    if (not) {
      builder.appendWord("NOT");
    }
    builder.appendWord(property);
    builder.appendWord(PLACE_HOLDER);
    builder.closeBracket();
  }

  @Override
  public int getArgsCount() {
    return 1;
  }

  @Override
  public Criterion not() {
    not = true;
    return this;
  }
}

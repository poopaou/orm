package org.medimob.orm.query;

import org.medimob.orm.internal.StatementBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by Poopaou on 30/01/2015.
 */
public class InCriterion implements NotCriterion {

  private final String property;
  private final Collection<Object> args;
  private boolean not;

  public InCriterion(String property, Collection<Object> args) {
    this.property = property;
    this.args = args;
  }

  @Override
  public void collectArgs(ArrayList<String> argsList) {
    for (Object o : args) {
      argsList.add(o.toString());
    }
  }

  @Override
  public void toSql(StatementBuilder builder, PropertyResolver resolver) {
    builder.appendWord(resolver.resolveColumnForProperty(property));
    if (not) {
      builder.appendWord("NOT");
    }
    builder.appendWord("IN");

    String[] strings = new String[args.size()];
    Arrays.fill(strings, PLACE_HOLDER);

    builder.openBracket();
    builder.appendWithSeparator(strings, ',');
    builder.closeBracket();
  }

  @Override
  public int getArgsCount() {
    return args.size();
  }

  @Override
  public Criterion not() {
    not = true;
    return this;
  }
}

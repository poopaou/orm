package org.medimob.orm.query;

import org.medimob.orm.internal.StatementBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Poopaou on 30/01/2015.
 */
public class SqlCriterion implements Criterion {

  private final String sql;
  private final List<String> args;

  public SqlCriterion(String sql, List<String> args) {
    this.sql = sql;
    this.args = args;
  }

  @Override
  public void collectArgs(ArrayList<String> argsList) {
    argsList.addAll(args);
  }

  @Override
  public void toSql(StatementBuilder builder, PropertyResolver resolver) {
    builder.appendWord(sql);
  }

  @Override
  public int getArgsCount() {
    return args.size();
  }
}

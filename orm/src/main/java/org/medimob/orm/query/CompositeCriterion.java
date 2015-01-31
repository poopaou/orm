package org.medimob.orm.query;

import org.medimob.orm.internal.StatementBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Poopaou on 30/01/2015.
 */
public class CompositeCriterion implements Criterion {

  private List<Criterion> subCriterion = new ArrayList<Criterion>();

  public void add(Criterion criterion) {
    subCriterion.add(criterion);
  }

  @Override
  public void collectArgs(ArrayList<String> argsList) {
    for (Criterion c : subCriterion) {
      c.collectArgs(argsList);
    }
  }

  @Override
  public void toSql(StatementBuilder builder, PropertyResolver resolver) {
    for (Criterion c : subCriterion) {
      c.toSql(builder, resolver);
    }
  }

  @Override
  public int getArgsCount() {
    int count = 0;
    for (Criterion c : subCriterion) {
      count += c.getArgsCount();
    }
    return count;
  }
}

package org.medimob.orm.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by Poopaou on 30/01/2015.
 */
public class Restrictions {

  public static Criterion eq(String property, Object value) {
    return new OperatorCriterion("==", property, value);
  }

  public static Criterion ne(String property, Object value) {
    return new OperatorCriterion("!=", property, value);
  }

  public static Criterion lt(String property, Object value) {
    return new OperatorCriterion("<", property, value);
  }

  public static Criterion le(String property, Object value) {
    return new OperatorCriterion("<=", property, value);
  }

  public static Criterion gt(String property, Object value) {
    return new OperatorCriterion(">", property, value);
  }

  public static Criterion ge(String property, Object value) {
    return new OperatorCriterion(">=", property, value);
  }

  public static NotCriterion like(String property, String value) {
    return new OperatorCriterion("LIKE", property, value);
  }

  public static NotCriterion glob(String property, String value) {
    return new OperatorCriterion("GLOB", property, value);
  }

  public static NotCriterion regexp(String property, String value) {
    return new OperatorCriterion("REGEXP", property, value);
  }

  public static NotCriterion between(String property, Object start, Object end) {
    return new BetweenCriterion(property, start, end);
  }

  public static Criterion isNull(String property) {
    return new ConstantCriterion("ISNULL", property);
  }

  public static Criterion isNotNull(String property) {
    return new ConstantCriterion("NOTNULL", property);
  }

  public static Criterion idEqual(long value) {
    return new IdCriterion(value);
  }

  public static NotCriterion in(String property, Collection values) {
    return new InCriterion(property, new ArrayList<Object>(values));
  }

  public static NotCriterion in(String property, Object... values) {
    return new InCriterion(property, Arrays.asList(values));
  }

  public static Criterion sqlRestriction(String sql, String... objects) {
    return new SqlCriterion(sql, Arrays.asList(objects));
  }

  public static void conjunction() {

  }

  public static void disjunction() {

  }
}

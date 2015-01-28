package org.medimob.orm.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * SQL 'check' definition. Can be ether used in @Table definition or the field definition. In this
 * case @Column check value will be override.
 */
@Documented
@Target(value = {ElementType.TYPE, ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Check {

  /**
   * Constraint name (only used in @Table definition).
   */
  String name();

  /**
   * Constraint SQL expression.
   */
  String exp();
}

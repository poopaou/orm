package org.medimob.orm.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Unique constraint definition.
 */
@Documented
@Target(value = {ElementType.FIELD, ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Unique {

  /**
   * @return constraint name.
   */
  String name() default "";

  /**
   * @return unique columns names (used only in @Table definition)
   */
  String[] columns();

  /**
   * @return the on clonflict clause  (default ROLLBACK).
   */
  Conflict onConflict() default Conflict.ROLLBACK;
}

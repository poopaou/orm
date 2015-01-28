package org.medimob.orm.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Index definition.
 */
@Documented
@Target(value = {ElementType.FIELD, ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Index {

  /**
   * Index's name (by default field(s) name is used).
   */
  String name() default "";

  /**
   * The index columns.
   */
  String[] columns();

  /**
   * Set to true if it's a unique index.
   */
  boolean unique() default false;

  /**
   * The index's where condition.
   */
  String where() default "";
}

package org.medimob.orm.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Table definition.
 */
@Documented
@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Table {

  /**
   * Set to true to create temporary table.
   */
  boolean temp() default false;

  /**
   * Tables unique constraints.
   */
  Unique[] uniques();

  /**
   * Tables check constraints.
   */
  Check[] checks();

  /**
   * Tables indexes.
   */
  Index[] indexes();

  /**
   * Tables triggers.
   */
  Trigger[] triggers();
}

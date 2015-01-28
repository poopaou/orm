package org.medimob.orm.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Trigger definition.
 */
@Documented
@Target(value = {ElementType.FIELD, ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Trigger {

  /**
   * Trigger's name.
   */
  String name();

  /**
   * Set to true for temporary trigger (default false).
   */
  boolean temp() default false;

  /**
   * Trigger's type.
   */
  TriggerType type();

  /**
   * Set to true if triggers apply for each row (default false).
   */
  boolean forEach() default false;

  /**
   * Trigger's when statements.
   */
  String when() default "";

  /**
   * Trigger's statements.
   */
  String[] statements();
}

package org.medimob.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.FIELD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Id {

  /**
   * Id's column's name default is "_id".
   */
  String name() default "_id";

  /**
   * Id's sort (default = Sort.NONE).
   */
  Sort sort() default Sort.NONE;

  /**
   * Auto incremented id value (default = true).
   */
  boolean autoIncrement() default true;

  /**
   * On conflict clause (default = Conflict.ROLLBACK).
   */
  Conflict onConflict() default Conflict.ROLLBACK;
}

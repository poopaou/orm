package org.medimob.orm.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Table column definition. Only basic type such as (int, String, long, double..) are supported.
 */
@Documented
@Target(value = ElementType.FIELD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Column {

  /**
   * Column's name. if not set the field name will be used.
   */
  String name() default "";

  /**
   * Set to true if the column can be null (default = false).
   */
  boolean notNull() default false;

  /**
   * Set to true if the column is unique  (default = false).
   */
  boolean unique() default false;

  /**
   * Set to true if the column is indexed  (default = false).
   */
  boolean indexed() default false;

  /**
   * The literal default value or and empty string.
   */
  String defaultValue() default "";

  /**
   * Column's collation name or and empty string.
   */
  String collate() default "";

  /**
   * SQL check expression or and empty string.
   */
  String check() default "";

  /**
   * Date field format. only apply for Date fields. By Default dates are persisted as long field
   * (Date.time value).
   */
  DateField dateType() default DateField.DATE_LONG;

  /**
   * Date format (only used if <code>dateType = DateField.DATE_String</code>). Stored Date format
   * pattern (default is "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
   */
  String dateFormat() default "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

  /**
   * Set to true if the column can be inserted (default = true).
   */
  boolean insertable() default true;

  /**
   * Set to true if the column can be updated (default = true).
   */
  boolean updatable() default true;
}

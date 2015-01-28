package org.medimob.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = {ElementType.FIELD, ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Trigger {

  String name() default "";

  boolean temp() default false;

  TriggerType type();

  boolean forEach() default false;

  String when() default "";

  String[] statements();
}

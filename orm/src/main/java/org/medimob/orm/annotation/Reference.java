package org.medimob.orm.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Column Reference constraint. Created by Poopaou on 02/02/2015.
 */
@Documented
@Target(value = ElementType.FIELD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Reference {

  Class<?> model();

  Action onUpdate() default Action.NO_ACTION;

  Action onDelete() default Action.NO_ACTION;

}

package org.medimob.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.FIELD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Id {
    String name() default "_id";
    Sort sort() default Sort.ASC;
    boolean autoIncrement() default true;
    Conflict onConflict() default Conflict.ROLLBACK;
}

package org.medimob.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Table {

    boolean temp() default false;
    Unique[] uniques();
    Check[] checks();
    Index[] indexes();
    Trigger[] triggers();
}

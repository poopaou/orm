package org.medimob.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.FIELD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Column {
    String name() default "";
    boolean notNull() default false;
    boolean unique() default false;
    boolean indexed() default false;
    String defaultValue() default "";
    String collate() default "";
    String check() default "";
    DateField dateType() default DateField.DATE_LONG;
    String dateFormat() default "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    boolean insertable() default true;
    boolean updatable() default true;

}

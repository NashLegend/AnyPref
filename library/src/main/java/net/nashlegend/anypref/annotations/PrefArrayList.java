package net.nashlegend.anypref.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by NashLegend on 16/6/3.
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface PrefArrayList {
    boolean nullable() default false;
    boolean itemNullable() default false;
}

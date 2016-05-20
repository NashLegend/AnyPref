package net.nashlegend.easypref.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Created by NashLegend on 16/5/20.
 */
@Target(TYPE)
@Retention(CLASS)
public @interface Pref {
    String[] value() default "";
}

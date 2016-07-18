package net.nashlegend.anypref.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by NashLegend on 16/5/20.
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface PrefModel {
    String value();
}

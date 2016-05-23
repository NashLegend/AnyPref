package net.nashlegend.demo.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by NashLegend on 16/5/23.
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface PrefIgnore {
}

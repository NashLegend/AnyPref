package net.nashlegend.anypref.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Created by NashLegend on 16/6/3.
 */
@Retention(CLASS)
@Target(TYPE)
public @interface PrefKeep {
}
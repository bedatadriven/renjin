package org.renjin.primitives.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that a match by class should be tried before
 * executing this default function.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface GroupGeneric {
  String value() default "";
}

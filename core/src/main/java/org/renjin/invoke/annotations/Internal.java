package org.renjin.invoke.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Defines the method as an internal R function that can be invoked with
 * .Internal(fn, ...). This is exists for source level compatibility with GNU R.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Internal {
  String value() default "";

}

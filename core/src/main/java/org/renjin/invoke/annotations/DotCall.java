package org.renjin.invoke.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Defines the method as an R function that can be invoked with
 * .Call(fn, ...). This is exists for source level compatibility with GNU R.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DotCall {
  String value() default "";
}

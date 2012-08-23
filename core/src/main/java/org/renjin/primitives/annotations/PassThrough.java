package org.renjin.primitives.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Calls to this method will be "passed through"
 * intact, without parsing or evaluating arguments.
 * The method is expected to have the signature
 * {@code (Context, Environment, FunctionCall)
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface PassThrough {
}

package org.renjin.primitives.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates than an argument should be passed, unevaluated, to 
 * as.character(). {@code NULL} values are handled specially, they
 * are <b>not</b> passed to {@code as.character} and will convert
 * to {@code NULL}
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface InvokeAsCharacter {
}

package org.renjin.invoke.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that this function has no side effects and its evaluation
 * may be safely deferred until a later time.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Deferrable {
}

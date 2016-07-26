package org.renjin.invoke.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the R NULL value is an acceptable value 
 * for this argument. 
 * 
 * <p>By default, NULL is accepted as an argument to a binary vectorized
 * function, for example {@code NULL+NULL}, but not to a unary function such as {@code sin(NULL)}</p>
 * 
 * <p>Annotating parameters with {@code @AllowNull} overrides this behavior and allows {@code NULL}
 * to be passed as the argument to a unary function.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface AllowNull {
}

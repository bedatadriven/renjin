package org.renjin.primitives.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * Can be used in conjunction with @Recycle (or implicit recycling)
 * to indicate that the attributes of the longest argument should
 * be copied to the result.
 * 
 * <p>If the attribute is absent, {@link PreserveAttributeStyle#SPECIAL} is 
 * assumed as default.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface PreserveAttributes {
  PreserveAttributeStyle value();
}

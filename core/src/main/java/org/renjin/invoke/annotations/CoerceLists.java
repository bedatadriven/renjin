package org.renjin.invoke.annotations;

/**
 * Indicates that an attempt should be made to coerce list arguments to an atomic
 * vector, AFTER attempting S3 dispatch but BEFORE matching types.
 */
public @interface CoerceLists {
}

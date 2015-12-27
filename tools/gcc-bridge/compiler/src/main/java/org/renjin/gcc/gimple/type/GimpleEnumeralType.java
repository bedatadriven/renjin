package org.renjin.gcc.gimple.type;

/**
 * GCC's enumeral_type, corresponds to something like:
 *
 * <code>
 * typedef enum { FALSE = 0, TRUE } Rboolean;
 * </code>
 *
 * in C. By the time it gets to gimple, it looks mostly like
 * a plain integer, so the plan for now is to treat it exactly
 * like an integer, we'll see later if this poses any problems.
 */
public class GimpleEnumeralType extends GimpleIntegerType {

}

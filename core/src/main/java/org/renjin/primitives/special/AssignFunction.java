package org.renjin.primitives.special;

/**
 * 
 * Implements the `=` function. 
 * 
 * <p>Note(ab): As far as I can tell, there is no functional difference
 * between the `<-` and `=` operators: the difference comes into play
 * at the level of the parser because the two have different associativity,
 * and `=` in the context of a function definition/call is actually not a 
 * syntatical structure. 
 * 
 * <p>For example:
 * <pre>
 * f <- function(x, na.rm = TRUE) {} 
 * </pre>
 * 
 * <p>is not an invocation of this function, but syntax.
 *
 */
public class AssignFunction extends AssignLeftFunction {

  public AssignFunction() {
    super("=");
  }
  
}

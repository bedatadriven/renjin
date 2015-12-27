package org.renjin.gcc.gimple.expr;


/**
 * GimpleLValue representing the value returned by a function. 
 * 
 * <p>When a value is assigned to a RESULT_DECL, that indicates that the value should be returned, via bitwise copy, 
 * by the function. 
 */
public class GimpleResultDecl extends GimpleLValue {

  @Override
  public String toString() {
    return "result_decl";
  }
}

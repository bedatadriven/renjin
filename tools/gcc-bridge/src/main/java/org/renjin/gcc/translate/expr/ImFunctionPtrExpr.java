package org.renjin.gcc.translate.expr;


import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.translate.FunctionContext;

/**
 * Interface to intermediate expressions which can be interpreted as
 * function pointers.
 */
public interface ImFunctionPtrExpr {

  /**
   * @return the jimple expression which evaluates to an instance of an invoker
   * for the function to which this expression points
   * @param context
   */
  JimpleExpr invokerReference(FunctionContext context);
}

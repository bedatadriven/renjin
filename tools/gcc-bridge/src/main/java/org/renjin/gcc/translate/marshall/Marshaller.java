package org.renjin.gcc.translate.marshall;


import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.expr.ImExpr;

/**
 * Translator that knows how to marshall an intermediate
 * expression to a return value or a call parameter.
 */
public interface Marshaller {
  
  JimpleExpr marshall(FunctionContext context, ImExpr expr);
  
}

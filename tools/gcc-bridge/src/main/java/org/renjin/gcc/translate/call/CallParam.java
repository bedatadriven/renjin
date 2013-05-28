package org.renjin.gcc.translate.call;

import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.expr.ImExpr;

public interface CallParam {

  JimpleExpr marshall(FunctionContext context, ImExpr expr);
}

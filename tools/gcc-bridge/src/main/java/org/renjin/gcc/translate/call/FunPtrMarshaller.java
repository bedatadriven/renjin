package org.renjin.gcc.translate.call;

import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleVar;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.var.FunPtrVar;
import org.renjin.gcc.translate.var.Variable;

public class FunPtrMarshaller extends ParamMarshaller {

  @Override
  public JimpleExpr marshall(FunctionContext context, GimpleExpr expr,
      CallParam param) {
    
    if(expr instanceof GimpleVar) {
      Variable var = context.lookupVar(expr);
      if(var instanceof FunPtrVar) {
        return ((FunPtrVar) var).getJimpleVariable();
      }
    }
    throw new CannotMarshallException();
  }

}

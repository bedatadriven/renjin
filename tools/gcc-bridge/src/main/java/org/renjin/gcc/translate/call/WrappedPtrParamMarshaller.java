package org.renjin.gcc.translate.call;

import org.renjin.gcc.gimple.expr.GimpleAddressOf;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleVar;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.var.Variable;

public class WrappedPtrParamMarshaller extends ParamMarshaller {

  @Override
  public JimpleExpr marshall(FunctionContext context, GimpleExpr expr,
      CallParam param) {
      
    if(param instanceof WrappedPtrCallParam) {
      if(expr instanceof GimpleVar) {
    
        Variable var = context.lookupVar(expr);
        return var.wrapPointer();      
      
      } else if(expr instanceof GimpleAddressOf) {
        GimpleExpr valueExpr = ((GimpleAddressOf) expr).getExpr();
        if(valueExpr instanceof GimpleVar) {
          Variable var = context.lookupVar(valueExpr);
          return var.wrapPointer();            
        }
       
      }   
    }
    throw new CannotMarshallException();

  }
  
}

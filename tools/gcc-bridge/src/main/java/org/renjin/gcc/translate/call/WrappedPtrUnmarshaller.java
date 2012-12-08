package org.renjin.gcc.translate.call;

import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleLValue;
import org.renjin.gcc.gimple.expr.GimpleVar;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.var.PrimitivePtrVar;
import org.renjin.gcc.translate.var.Variable;

public class WrappedPtrUnmarshaller extends CallUnmarshaller {

  
  
  @Override
  public boolean unmarshall(FunctionContext context, GimpleLValue lhs,
      JimpleType returnType, JimpleExpr callExpr) {
    
    if(lhs instanceof GimpleVar) {
      Variable var = context.lookupVar(lhs);
      if(var instanceof PrimitivePtrVar) {
        PrimitivePtrVar ptrVar = (PrimitivePtrVar) var;
        if(returnType.isPointerWrapper()) {
          String temp = context.getBuilder().addTempVarDecl(returnType);
          context.getBuilder().addStatement(temp + " = " + callExpr);
          ptrVar.assignFromWrapper(new JimpleExpr(temp));
          return true;
        }
      }
    }
    return false;
    
  }

  public boolean accept(FunctionContext context, GimpleExpr lhs, MethodRef method) {
    if( ! (lhs instanceof GimpleVar) ) {
      return false;
    }
    Variable var = context.lookupVar(lhs);
    return var instanceof PrimitivePtrVar && method.getReturnType().isPointerWrapper();
  }
  
  public void unmarshall() {
    
  }
  
}

package org.renjin.gcc.codegen.type.fun;

import com.google.common.base.Optional;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.call.MethodHandleGenerator;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.expr.SimpleLValue;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleParameter;

import java.lang.invoke.MethodHandle;
import java.util.Collections;
import java.util.List;

import static org.renjin.gcc.codegen.type.fun.FunPtrStrategy.METHOD_HANDLE_TYPE;

public class FunPtrParamStrategy implements ParamStrategy {

  @Override
  public List<Type> getParameterTypes() {
    return Collections.singletonList(METHOD_HANDLE_TYPE);
  }

  @Override
  public Expr emitInitialization(MethodGenerator methodVisitor, GimpleParameter parameter, List<SimpleLValue> paramVars, VarAllocator localVars) {
    if(parameter.isAddressable()) {
      throw new UnsupportedOperationException("TODO: Addressable parameters");
    }

    return paramVars.get(0);  
  }

  @Override
  public void loadParameter(MethodGenerator mv, Optional<Expr> argument) {
  
    if(argument.isPresent()) {
      SimpleExpr ptrValue = (SimpleExpr) argument.get();
      ptrValue.load(mv);
      if(!ptrValue.getType().equals(METHOD_HANDLE_TYPE)) {
        mv.cast(ptrValue.getType(), METHOD_HANDLE_TYPE);
      }
    } else {
      mv.aconst(null);
    }
  }
}

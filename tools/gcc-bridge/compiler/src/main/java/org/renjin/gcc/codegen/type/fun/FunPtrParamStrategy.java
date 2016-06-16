package org.renjin.gcc.codegen.type.fun;

import com.google.common.base.Optional;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleParameter;

import java.util.Collections;
import java.util.List;

import static org.renjin.gcc.codegen.type.fun.FunPtrStrategy.METHOD_HANDLE_TYPE;

public class FunPtrParamStrategy implements ParamStrategy {

  @Override
  public List<Type> getParameterTypes() {
    return Collections.singletonList(METHOD_HANDLE_TYPE);
  }

  @Override
  public FunPtr emitInitialization(MethodGenerator methodVisitor, GimpleParameter parameter, List<JLValue> paramVars, VarAllocator localVars) {
    if(parameter.isAddressable()) {
      throw new UnsupportedOperationException("TODO: Addressable parameters");
    }

    return new FunPtr(paramVars.get(0));
  }

  @Override
  public void loadParameter(MethodGenerator mv, Optional<GExpr> argument) {
  
    if(argument.isPresent()) {
      FunPtr ptrValue = (FunPtr) argument.get();
      ptrValue.unwrap().load(mv);
    } else {
      mv.aconst(null);
    }
  }
}

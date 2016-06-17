package org.renjin.gcc.codegen.type.voidt;

import com.google.common.base.Optional;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.expr.RefPtrExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleParameter;

import java.util.Collections;
import java.util.List;

/**
 * Strategy for passing {@code void*} parameters
 */
public class VoidPtrParamStrategy implements ParamStrategy {
  @Override
  public List<Type> getParameterTypes() {
    return Collections.singletonList(Type.getType(Object.class));
  }

  @Override
  public GExpr emitInitialization(MethodGenerator methodVisitor, GimpleParameter parameter, List<JLValue> paramVars, VarAllocator localVars) {
    if(parameter.isAddressable()) {
      throw new UnsupportedOperationException("TODO: Addressable void pointer parameters");
    }
    return new VoidPtr(paramVars.get(0));
  }

  @Override
  public void loadParameter(MethodGenerator mv, Optional<GExpr> argument) {
    
    if(argument.isPresent()) {
      GExpr argumentValue = argument.get();
      if (argumentValue instanceof FatPtrExpr) {
        ((FatPtrExpr) argumentValue).wrap().load(mv);

      } else if (argumentValue instanceof RefPtrExpr) {
        RefPtrExpr ptr = (RefPtrExpr) argumentValue;
        ptr.unwrap().load(mv);
      }
    } else {
      // Argument not supplied
      mv.aconst(null);
    }
  }
}

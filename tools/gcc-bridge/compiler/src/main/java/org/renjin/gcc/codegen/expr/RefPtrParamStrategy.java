package org.renjin.gcc.codegen.expr;

import com.google.common.base.Optional;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.fatptr.FatPtr;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.type.SimpleTypeStrategy;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.repackaged.asm.Type;

import java.util.Collections;
import java.util.List;

public class RefPtrParamStrategy<T extends RefPtrExpr> implements ParamStrategy {
  
  private SimpleTypeStrategy<T> typeStrategy;

  public RefPtrParamStrategy(SimpleTypeStrategy<T> typeStrategy) {
    this.typeStrategy = typeStrategy;
  }

  @Override
  public List<Type> getParameterTypes() {
    return Collections.singletonList(typeStrategy.getJvmType());
  }

  @Override
  public GExpr emitInitialization(MethodGenerator methodVisitor, GimpleParameter parameter, 
                                  List<JLValue> paramVars, VarAllocator localVars) {
    if(parameter.isAddressable()) {
      throw new UnsupportedOperationException("TODO: Addressable parameters");
    }

    return typeStrategy.wrap(paramVars.get(0));
  }

  @Override
  public void loadParameter(MethodGenerator mv, Optional<GExpr> argument) {
    if(argument.isPresent()) {
      
      GExpr argumentValue = argument.get();
      if(argumentValue instanceof RefPtrExpr) {
        RefPtrExpr ptrExpr = (RefPtrExpr) argument.get();

        if (ptrExpr.unwrap().getType().equals(typeStrategy.getJvmType())) {
          ptrExpr.unwrap().load(mv);

        } else {
          // Cast null pointers to the appropriate type
          Expressions.cast(ptrExpr.unwrap(), typeStrategy.getJvmType()).load(mv);
        }
      } else if(argumentValue instanceof FatPtr) {
        FatPtr fatPtrExpr = (FatPtr) argumentValue;
        fatPtrExpr.wrap().load(mv);
        
      } else {
        throw new UnsupportedOperationException("argument type: " + argumentValue);
      }
    } else {
      mv.aconst(null);
    }
  }
}

package org.renjin.gcc.codegen.type.record.unit;

import com.google.common.base.Optional;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.expr.RefPtrExpr;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleParameter;

import java.util.Collections;
import java.util.List;

public class RecordUnitPtrParamStrategy implements ParamStrategy {
  
  private Type jvmType;

  public RecordUnitPtrParamStrategy(Type jvmType) {
    this.jvmType = jvmType;
  }

  @Override
  public List<Type> getParameterTypes() {
    return Collections.singletonList(jvmType);
  }

  @Override
  public GExpr emitInitialization(MethodGenerator methodVisitor, GimpleParameter parameter, List<JLValue> paramVars, VarAllocator localVars) {
    if(parameter.isAddressable()) {
      throw new UnsupportedOperationException("TODO: Addressable parameters");
    }

    return new RecordUnitPtr(paramVars.get(0));
  }

  @Override
  public void loadParameter(MethodGenerator mv, Optional<GExpr> argument) {
    if(argument.isPresent()) {
      
      GExpr argumentValue = argument.get();
      if(argumentValue instanceof RefPtrExpr) {
        RefPtrExpr value = (RefPtrExpr) argument.get();

        if (value.unwrap().getType().equals(jvmType)) {
          value.unwrap().load(mv);

        } else {
          // Cast null pointers to the appropriate type
          Expressions.cast(value.unwrap(), this.jvmType).load(mv);
        }
      }
    } else {
      mv.aconst(null);
    }
  }
}

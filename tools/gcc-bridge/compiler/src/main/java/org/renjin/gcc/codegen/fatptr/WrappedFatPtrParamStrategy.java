package org.renjin.gcc.codegen.fatptr;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.repackaged.asm.Type;

import java.util.List;

/**
 * Strategy for using FatPtrs as a single wrapped fat pointer
 */
public class WrappedFatPtrParamStrategy implements ParamStrategy {

  private ValueFunction valueFunction;

  public WrappedFatPtrParamStrategy(ValueFunction valueFunction) {
    this.valueFunction = valueFunction;
  }

  @Override
  public List<Type> getParameterTypes() {
    return Lists.newArrayList(Wrappers.wrapperType(valueFunction.getValueType()));
  }

  @Override
  public GExpr emitInitialization(MethodGenerator mv, GimpleParameter parameter, List<JLValue> paramVars, VarAllocator localVars) {
    JLValue array = localVars.reserveArrayRef(parameter.getName() + "$array", valueFunction.getValueType());
    JLValue offset = localVars.reserveInt(parameter.getName() + "$offset");

    JExpr wrapper = paramVars.get(0);
    JExpr arrayField = Wrappers.arrayField(wrapper, valueFunction.getValueType());
    JExpr offsetField = Wrappers.offsetField(wrapper);

    if(valueFunction.getValueType().getSort() == Type.OBJECT) {
      arrayField = Expressions.cast(arrayField, Wrappers.valueArrayType(valueFunction.getValueType()));
    }

    array.store(mv, arrayField);
    offset.store(mv, offsetField);
    
    return new FatPtrExpr(array, offset);
  }

  @Override
  public void loadParameter(MethodGenerator mv, Optional<GExpr> argument) {
    
    if(!argument.isPresent()) {
      mv.aconst(null);
      return;
    }
    
    GExpr argumentValue = argument.get();
    
    // Check for a void*
    if(argumentValue instanceof RefPtrExpr) {
      RefPtrExpr refPtr = (RefPtrExpr) argumentValue;
      JExpr wrappedPtr = Expressions.cast(refPtr.unwrap(), Wrappers.wrapperType(valueFunction.getValueType()));
      wrappedPtr.load(mv);
    
    } else if(argumentValue instanceof FatPtrExpr) {
      FatPtrExpr fatPtrExpr = (FatPtrExpr) argumentValue;
      fatPtrExpr.wrap().load(mv);  
   
    } else {
      throw new IllegalArgumentException("argument: " + argumentValue);
    }
  }
}

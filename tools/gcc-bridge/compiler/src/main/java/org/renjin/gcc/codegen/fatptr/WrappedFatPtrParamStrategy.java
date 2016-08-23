package org.renjin.gcc.codegen.fatptr;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Optional;
import org.renjin.repackaged.guava.collect.Lists;

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
    
    if(valueFunction.getValueType().getSort() == Type.OBJECT) {
      return new WrappedFatPtrExpr(valueFunction, paramVars.get(0));

    } else {
      
      // For pointers to primitive, unpack for efficiency
      
      JLValue array = localVars.reserve(parameter.getName() + "$array", Wrappers.valueArrayType(valueFunction.getValueType()));
      JLValue offset = localVars.reserveInt(parameter.getName() + "$offset");

      JExpr wrapper = paramVars.get(0);
      JExpr arrayField = Wrappers.arrayField(wrapper, valueFunction.getValueType());
      JExpr offsetField = Wrappers.offsetField(wrapper);

      array.store(mv, arrayField);
      offset.store(mv, offsetField);

      return new FatPtrPair(valueFunction, array, offset);
    }
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
    
    } else if(argumentValue instanceof FatPtr) {
      FatPtr fatPtrExpr = (FatPtr) argumentValue;
      fatPtrExpr.wrap().load(mv);

    } else {
      throw new IllegalArgumentException("argument: " + argumentValue);
    }
  }
}

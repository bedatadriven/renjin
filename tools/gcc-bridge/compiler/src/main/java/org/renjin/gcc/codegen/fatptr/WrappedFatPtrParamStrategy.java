package org.renjin.gcc.codegen.fatptr;

import com.google.common.collect.Lists;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.expr.SimpleLValue;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleParameter;

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
  public Expr emitInitialization(MethodGenerator mv, GimpleParameter parameter, List<SimpleLValue> paramVars, VarAllocator localVars) {
    SimpleLValue array = localVars.reserveArrayRef(parameter.getName() + "$array", valueFunction.getValueType());
    SimpleLValue offset = localVars.reserveInt(parameter.getName() + "$offset");

    SimpleExpr wrapper = paramVars.get(0);
    SimpleExpr arrayField = Wrappers.arrayField(wrapper, valueFunction.getValueType());
    SimpleExpr offsetField = Wrappers.offsetField(wrapper);

    if(valueFunction.getValueType().getSort() == Type.OBJECT) {
      arrayField = Expressions.cast(arrayField, Wrappers.valueArrayType(valueFunction.getValueType()));
    }

    array.store(mv, arrayField);
    offset.store(mv, offsetField);
    
    return new FatPtrExpr(array, offset);
  }

  @Override
  public void loadParameter(MethodGenerator mv, Expr argument) {
    
    // Check for a void*
    if(argument instanceof SimpleExpr) {
      SimpleExpr wrappedPtr = Expressions.cast((SimpleExpr) argument, Wrappers.wrapperType(valueFunction.getValueType()));
      wrappedPtr.load(mv);
    
    } else if(argument instanceof FatPtrExpr) {
      FatPtrExpr fatPtrExpr = (FatPtrExpr) argument;
      fatPtrExpr.wrap().load(mv);  
   
    } else {
      throw new IllegalArgumentException("argument: " + argument);
    }
  }
}

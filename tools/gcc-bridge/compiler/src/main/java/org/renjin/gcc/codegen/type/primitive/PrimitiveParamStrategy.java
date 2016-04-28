package org.renjin.gcc.codegen.type.primitive;

import com.google.common.base.Optional;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleParameter;

import java.util.Collections;
import java.util.List;

/**
 * Strategy for passing and receiving parameters of primitive type.
 */
public class PrimitiveParamStrategy implements ParamStrategy {
  
  private Type type;

  public PrimitiveParamStrategy(Type type) {
    this.type = type;
  }

  @Override
  public List<Type> getParameterTypes() {
    return Collections.singletonList(type);
  }

  @Override
  public Expr emitInitialization(MethodGenerator mv, 
                                 GimpleParameter parameter, 
                                 List<SimpleLValue> paramVars, 
                                 VarAllocator localVars) {
    
    SimpleExpr paramValue = paramVars.get(0);

    if(parameter.isAddressable()) {
      // if this parameter is addressed, then we need to allocate a unit array that can hold the value
      // and be addressed as needed.
      SimpleLValue unitArray = localVars.reserveUnitArray(parameter.getName(), type, Optional.of(paramValue));
      FatPtrExpr address = new FatPtrExpr(unitArray);
      SimpleExpr value = Expressions.elementAt(address.getArray(), 0);
      return new SimpleAddressableExpr(value, address);
    } else {
      
      // Otherwise we can just reference the value of the parameter
      return paramValue;
      
    }
  }

  @Override
  public void loadParameter(MethodGenerator mv, Expr argument) {
    ((SimpleExpr) argument).load(mv);
  }
}

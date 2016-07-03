package org.renjin.gcc.codegen.type.primitive;

import com.google.common.base.Optional;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.repackaged.asm.Type;

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
  public GExpr emitInitialization(MethodGenerator mv,
                                  GimpleParameter parameter,
                                  List<JLValue> paramVars,
                                  VarAllocator localVars) {
    
    JExpr paramValue = paramVars.get(0);

    if(parameter.isAddressable()) {
      // if this parameter is addressed, then we need to allocate a unit array that can hold the value
      // and be addressed as needed.
      JLValue unitArray = localVars.reserveUnitArray(parameter.getName(), type, Optional.of(paramValue));
      FatPtrExpr address = new FatPtrExpr(unitArray);
      JExpr value = Expressions.elementAt(address.getArray(), 0);
      return new PrimitiveValue(value, address);
    } else {
      
      // Otherwise we can just reference the value of the parameter
      return new PrimitiveValue(paramValue);
    }
  }

  @Override
  public void loadParameter(MethodGenerator mv, Optional<GExpr> argument) {
    if(argument.isPresent()) {
      ((PrimitiveValue) argument.get()).getExpr().load(mv);
    } else {
      new ConstantValue(type, 0).load(mv);
    }
  }
}

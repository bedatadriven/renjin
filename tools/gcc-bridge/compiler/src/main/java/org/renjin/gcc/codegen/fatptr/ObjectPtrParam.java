package org.renjin.gcc.codegen.fatptr;

import com.google.common.base.Optional;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.runtime.ObjectPtr;
import org.renjin.repackaged.asm.Type;

import java.util.Collections;
import java.util.List;


public class ObjectPtrParam implements ParamStrategy {
  
  private ValueFunction valueFunction;

  public ObjectPtrParam(ValueFunction valueFunction) {
    this.valueFunction = valueFunction;
  }

  @Override
  public List<Type> getParameterTypes() {
    return Collections.singletonList(Type.getType(ObjectPtr.class));
  }

  @Override
  public GExpr emitInitialization(MethodGenerator methodVisitor, GimpleParameter parameter, 
                                  List<JLValue> paramVars, VarAllocator localVars) {
    return new ObjectFatPtrParam(valueFunction, paramVars.get(0));
  }

  @Override
  public void loadParameter(MethodGenerator mv, Optional<GExpr> argument) {
    new WrappedFatPtrParamStrategy(valueFunction).loadParameter(mv, argument);
  }
}

package org.renjin.gcc.codegen.type.voidt;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.expr.SimpleLValue;
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
  public Expr emitInitialization(MethodGenerator methodVisitor, GimpleParameter parameter, List<SimpleLValue> paramVars, VarAllocator localVars) {
    return paramVars.get(0);
  }

  @Override
  public void loadParameter(MethodGenerator mv, Expr argument) {
    if(argument instanceof FatPtrExpr) {
      ((FatPtrExpr) argument).wrap().load(mv);
    
    } else if(argument instanceof SimpleExpr) {
      SimpleExpr simpleArgument = (SimpleExpr) argument;
      if(simpleArgument.getType().getSort() != Type.OBJECT) {
        throw new IllegalArgumentException("not an object: " + argument);
      }
      simpleArgument.load(mv);
    }
  }
}

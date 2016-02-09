package org.renjin.gcc.codegen.type;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.expr.SimpleLValue;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleParameter;

import java.util.Collections;
import java.util.List;

/**
 * Strategy for parameters that can be represented as a {@link SimpleExpr}
 */
public class SimpleParamStrategy implements ParamStrategy {
  
  private final Type type;

  public SimpleParamStrategy(Type type) {
    this.type = type;
  }

  @Override
  public List<Type> getParameterTypes() {
    return Collections.singletonList(type);
  }

  @Override
  public Expr emitInitialization(MethodGenerator methodVisitor, GimpleParameter parameter, List<SimpleLValue> paramVars, VarAllocator localVars) {
    return paramVars.get(0);
  }

  @Override
  public void loadParameter(MethodGenerator mv, Expr argument) {
    SimpleExpr value = (SimpleExpr) argument;
    
    if(value.getType().equals(type)) {
      value.load(mv);
    
    } else if(value.getType().equals(Type.getType(Object.class))) {
      // Cast null pointers to the appropriate type
      Expressions.cast(value, this.type).load(mv);
    
    } else {
      // Try to cast to the right type
      // TODO: handle this more systematically
      if (this.type.equals(Type.BOOLEAN_TYPE) && value.getType().equals(Type.INT_TYPE)) {
        value.load(mv);
      } else {
        throw new IllegalArgumentException(String.format("expected argument type: %s, found: %s",
            this.type,
            value.getType()));
      }
    }
  }
}

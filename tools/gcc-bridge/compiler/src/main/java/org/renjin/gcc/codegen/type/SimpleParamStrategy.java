package org.renjin.gcc.codegen.type;

import com.google.common.base.Preconditions;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expr;
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
  public void emitPushParameter(MethodGenerator mv, Expr param) {
    SimpleExpr value = (SimpleExpr) param;
    Preconditions.checkArgument(value.getType().equals(type));
    value.load(mv);
  }
}

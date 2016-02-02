package org.renjin.gcc.codegen.type;

import com.google.common.base.Preconditions;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.var.Value;
import org.renjin.gcc.codegen.var.Var;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleParameter;

import java.util.Collections;
import java.util.List;

public class ValueParamStrategy implements ParamStrategy {
  
  private final Type type;

  public ValueParamStrategy(Type type) {
    this.type = type;
  }

  @Override
  public List<Type> getParameterTypes() {
    return Collections.singletonList(type);
  }

  @Override
  public ExprGenerator emitInitialization(MethodGenerator methodVisitor, GimpleParameter parameter, List<Var> paramVars, VarAllocator localVars) {
    return paramVars.get(0);
  }

  @Override
  public void emitPushParameter(MethodGenerator mv, ExprGenerator param) {
    Value value = (Value) param;
    Preconditions.checkArgument(value.getType().equals(type));
    value.load(mv);
  }
}

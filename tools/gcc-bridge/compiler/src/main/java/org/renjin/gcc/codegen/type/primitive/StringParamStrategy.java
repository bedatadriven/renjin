package org.renjin.gcc.codegen.type.primitive;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.var.Var;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleParameter;

import java.util.Collections;
import java.util.List;

/**
 * Strategy for {@code String} parameters, used only for interfacing with external JVM methods.
 */
public class StringParamStrategy implements ParamStrategy {
  @Override
  public List<Type> getParameterTypes() {
    return Collections.singletonList(Type.getType(String.class));
  }

  @Override
  public ExprGenerator emitInitialization(MethodGenerator methodVisitor, GimpleParameter parameter, List<Var> paramVars, VarAllocator localVars) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void emitPushParameter(MethodGenerator mv, ExprGenerator valueGenerator) {
    throw new UnsupportedOperationException();
  }

}

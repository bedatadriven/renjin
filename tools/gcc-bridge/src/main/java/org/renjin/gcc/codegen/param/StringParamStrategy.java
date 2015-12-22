package org.renjin.gcc.codegen.param;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.LocalVarAllocator;
import org.renjin.gcc.codegen.Var;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.GimpleParameter;

import java.util.Collections;
import java.util.List;

/**
 * Strategy for {@code String} parameters, used only for interfacing with external JVM methods.
 */
public class StringParamStrategy extends ParamStrategy {
  @Override
  public List<Type> getParameterTypes() {
    return Collections.singletonList(Type.getType(String.class));
  }

  @Override
  public ExprGenerator emitInitialization(MethodVisitor methodVisitor, GimpleParameter parameter, List<Var> paramVars, LocalVarAllocator localVars) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void emitPushParameter(MethodVisitor mv, ExprGenerator valueGenerator) {
    throw new UnsupportedOperationException();
  }

}

package org.renjin.gcc.codegen.call;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.type.ReturnStrategy;
import org.renjin.gcc.codegen.type.voidt.VoidReturnStrategy;
import org.renjin.gcc.runtime.Builtins;

import java.util.Collections;
import java.util.List;

import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;

/**
 * Throws a runtime exception.
 */
public class UnimplCallGenerator implements InvocationStrategy {

  private String functionName;

  public UnimplCallGenerator(String functionName) {
    this.functionName = functionName;
  }

  @Override
  public Handle getMethodHandle() {
    return new Handle(H_INVOKESTATIC, Type.getInternalName(Builtins.class), "undefined_std", "()V");
  }

  @Override
  public List<ParamStrategy> getParamStrategies() {
    return Collections.emptyList();
  }

  @Override
  public boolean isVarArgs() {
    return false;
  }

  @Override
  public ReturnStrategy getReturnStrategy() {
    return new VoidReturnStrategy();
  }

  @Override
  public void invoke(MethodGenerator mv) {
    mv.invokestatic(Builtins.class, "undefined_std", "()V");
  }
}

package org.renjin.compiler.codegen;

import org.renjin.eval.Context;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.Environment;
import org.renjin.sexp.Function;

public class NamedFunctionLoader implements FunctionLoader {

  private final String name;

  public NamedFunctionLoader(String name) {
    this.name = name;
  }

  @Override
  public void loadFunction(EmitContext context, InstructionAdapter mv) {
    mv.visitVarInsn(Opcodes.ALOAD, context.getContextVarIndex());
    mv.visitVarInsn(Opcodes.ALOAD, context.getEnvironmentVarIndex());
    mv.aconst(name);
    mv.invokevirtual(Type.getInternalName(Context.class), "evaluateFunction",
        Type.getMethodDescriptor(Type.getType(Function.class),
            Type.getType(Environment.class),
            Type.getType(String.class)), false);
  }
}

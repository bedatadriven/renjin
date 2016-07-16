package org.renjin.compiler.pipeline.accessor;

import org.renjin.compiler.pipeline.ComputeMethod;
import org.renjin.repackaged.asm.MethodVisitor;
import org.renjin.repackaged.asm.Opcodes;

import static org.renjin.repackaged.asm.Opcodes.D2I;

public abstract class Accessor {

  public abstract void init(ComputeMethod method);

  /**
   * The index is on the stack, the method should
   * push the corresponding double on to the stack.
   * @param method
   */
  public abstract void pushDouble(ComputeMethod method);

  public abstract void pushLength(ComputeMethod method);

  protected final void pushOperandIndex(MethodVisitor mv, int operandIndex) {
    if (operandIndex >= -1 && operandIndex <= 5) {
      mv.visitInsn(Opcodes.ICONST_0 + operandIndex);
    } else if (operandIndex >= Byte.MIN_VALUE && operandIndex <= Byte.MAX_VALUE) {
      mv.visitIntInsn(Opcodes.BIPUSH, operandIndex);
    } else if (operandIndex >= Short.MIN_VALUE && operandIndex <= Short.MAX_VALUE) {
      mv.visitIntInsn(Opcodes.SIPUSH, operandIndex);
    } else {
      mv.visitLdcInsn(operandIndex);
    }
  }

  public void pushInt(ComputeMethod method) {
    pushDouble(method);
    method.getVisitor().visitInsn(D2I);
  }

}

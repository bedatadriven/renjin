package org.renjin.compiler.pipeline.accessor;

import org.objectweb.asm.MethodVisitor;
import org.renjin.compiler.pipeline.ComputeMethod;

import static org.objectweb.asm.Opcodes.*;

public abstract class Accessor {

  public abstract void init(ComputeMethod method);

  /**
   * The index is on the stack, the method should
   * push the corresponding double on to the stack.
   * @param method
   */
  public void pushDouble(ComputeMethod method) {


  }

  public abstract void pushLength(ComputeMethod method);


  protected final void pushOperandIndex(MethodVisitor mv, int operandIndex) {
    if(operandIndex == 0) {
      mv.visitInsn(ICONST_0);
    } else if(operandIndex == 1) {
      mv.visitInsn(ICONST_1);
    } else if(operandIndex == 2) {
      mv.visitInsn(ICONST_2);
    } else if(operandIndex == 3) {
      mv.visitInsn(ICONST_3);
    } else if(operandIndex == 4) {
      mv.visitInsn(ICONST_4);
    } else if(operandIndex == 5) {
      mv.visitInsn(ICONST_5);
    } else if(operandIndex < Byte.MAX_VALUE) {
      mv.visitIntInsn(BIPUSH, operandIndex);
    } else {
      throw new UnsupportedOperationException("operandIndex: " + operandIndex);
    }
  }

  public void pushInt(ComputeMethod method) {
    pushDouble(method);
    method.getVisitor().visitInsn(D2I);
  }

}

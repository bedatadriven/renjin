package org.renjin.compiler.pipeline.fusion;

import org.renjin.compiler.pipeline.ComputeMethod;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.MethodVisitor;
import org.renjin.repackaged.guava.base.Optional;

import static org.renjin.repackaged.asm.Opcodes.*;

public class DoubleArrayAccessor extends Accessor {

  /**
   * The local variable where we're storing the
   * raw array, double[]
   */
  private int arrayLocalIndex;
  private int operandIndex;

  public DoubleArrayAccessor(int operandIndex) {
    this.operandIndex = operandIndex;
  }

  public void init(ComputeMethod method) {

    arrayLocalIndex = method.reserveLocal(1);

    MethodVisitor mv = method.getVisitor();
    mv.visitVarInsn(ALOAD, method.getOperandsLocalIndex());
    pushIntConstant(mv, operandIndex);
    mv.visitInsn(AALOAD);
    mv.visitTypeInsn(CHECKCAST, "org/renjin/sexp/DoubleArrayVector");
    mv.visitMethodInsn(INVOKEVIRTUAL, "org/renjin/sexp/DoubleArrayVector", "toDoubleArrayUnsafe", "()[D");
    mv.visitVarInsn(ASTORE, arrayLocalIndex);
  }

  @Override
  public void pushLength(ComputeMethod method) {
    MethodVisitor mv = method.getVisitor();
    mv.visitVarInsn(ALOAD, arrayLocalIndex);
    mv.visitInsn(ARRAYLENGTH);
  }

  @Override
  public boolean mustCheckForIntegerNAs() {
    return false;
  }

  @Override
  public void pushElementAsDouble(ComputeMethod method, Optional<Label> integerNaLabel) {
    MethodVisitor mv = method.getVisitor();
    mv.visitVarInsn(ALOAD, arrayLocalIndex);
    mv.visitInsn(SWAP);
    mv.visitInsn(DALOAD);
  }
}

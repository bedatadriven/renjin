package org.renjin.compiler.pipeline.fusion;

import org.renjin.compiler.pipeline.ComputeMethod;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.MethodVisitor;
import org.renjin.repackaged.guava.base.Optional;

import static org.renjin.repackaged.asm.Opcodes.*;

public class IntArrayAccessor extends Accessor {

  /**
   * The local variable where we're storing the
   * raw array, double[]
   */
  private int arrayLocalIndex;
  private int operandIndex;

  public IntArrayAccessor(int operandIndex) {
    this.operandIndex = operandIndex;
  }

  public void init(ComputeMethod method) {

    arrayLocalIndex = method.reserveLocal(1);

    MethodVisitor mv = method.getVisitor();
    mv.visitVarInsn(ALOAD, method.getOperandsLocalIndex());
    pushIntConstant(mv, operandIndex);
    mv.visitInsn(AALOAD);
    mv.visitTypeInsn(CHECKCAST, "org/renjin/sexp/IntArrayVector");
    mv.visitMethodInsn(INVOKEVIRTUAL, "org/renjin/sexp/IntArrayVector", "toIntArrayUnsafe", "()[I");
    mv.visitVarInsn(ASTORE, arrayLocalIndex);
  }

  @Override
  public void pushLength(ComputeMethod method) {
    MethodVisitor mv = method.getVisitor();
    mv.visitVarInsn(ALOAD, arrayLocalIndex);
    mv.visitInsn(ARRAYLENGTH);
  }

  @Override
  public void pushElementAsDouble(ComputeMethod method, Optional<Label> integerNaLabel) {
    pushElementAsInt(method, integerNaLabel);
    MethodVisitor mv = method.getVisitor();
    mv.visitInsn(I2D);
  }

  @Override
  public void pushElementAsInt(ComputeMethod method, Optional<Label> integerNaLabel) {
    MethodVisitor mv = method.getVisitor();
    mv.visitVarInsn(ALOAD, arrayLocalIndex);
    mv.visitInsn(SWAP);
    mv.visitInsn(IALOAD);
    doIntegerNaCheck(mv, integerNaLabel);
  }

  @Override
  public boolean mustCheckForIntegerNAs() {
    return true;
  }

}

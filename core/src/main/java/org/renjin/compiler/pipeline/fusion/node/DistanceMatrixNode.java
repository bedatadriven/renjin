package org.renjin.compiler.pipeline.fusion.node;

import org.renjin.compiler.pipeline.ComputeMethod;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.MethodVisitor;
import org.renjin.repackaged.guava.base.Optional;

import static org.renjin.repackaged.asm.Opcodes.*;

public class DistanceMatrixNode extends LoopNode {
  private LoopNode operandNode;
  private int indexTempLocal;
  private int rowTempLocal;
  private int colTempLocal;

  public DistanceMatrixNode(LoopNode operandNode) {
    this.operandNode = operandNode;
  }

  @Override
  public void init(ComputeMethod method) {
    this.operandNode.init(method);
    indexTempLocal = method.reserveLocal(1);
    rowTempLocal = method.reserveLocal(1);
    colTempLocal = method.reserveLocal(1);

  }

  @Override
  public void pushLength(ComputeMethod method) {
    operandNode.pushLength(method);
    MethodVisitor mv = method.getVisitor();
    mv.visitInsn(DUP);
    mv.visitInsn(IMUL);
  }

  @Override
  public boolean mustCheckForIntegerNAs() {
    return operandNode.mustCheckForIntegerNAs();
  }

  @Override
  public void pushElementAsDouble(ComputeMethod method, Optional<Label> integerNaLabel) {
    MethodVisitor mv = method.getVisitor();

    mv.visitInsn(DUP);
    mv.visitVarInsn(ISTORE, indexTempLocal);

    // row = index % length
    operandNode.pushLength(method);
    mv.visitInsn(IREM);

    mv.visitVarInsn(ISTORE, rowTempLocal);

    // col = index / length
    mv.visitVarInsn(ILOAD, indexTempLocal);
    operandNode.pushLength(method);
    mv.visitInsn(IDIV);
    mv.visitVarInsn(ISTORE, colTempLocal);


    // push x[row]
    mv.visitVarInsn(ILOAD, rowTempLocal);
    operandNode.pushElementAsDouble(method, integerNaLabel);

    // push x[col]
    mv.visitVarInsn(ILOAD, colTempLocal);
    operandNode.pushElementAsDouble(method, integerNaLabel);

    // x[row] - x[col]
    mv.visitInsn(DSUB);
    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "abs", "(D)D", false);
  }
}

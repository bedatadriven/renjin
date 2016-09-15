package org.renjin.compiler.pipeline.fusion;

import org.renjin.compiler.pipeline.ComputeMethod;
import org.renjin.compiler.pipeline.node.DeferredNode;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.MethodVisitor;
import org.renjin.repackaged.guava.base.Optional;

import static org.renjin.repackaged.asm.Opcodes.*;

public class DistanceMatrixAccessor extends Accessor {
  private int operandIndex;
  private Accessor operandAccessor;
  private int indexTempLocal;
  private int rowTempLocal;
  private int colTempLocal;

  public DistanceMatrixAccessor(DeferredNode node, InputGraph inputGraph) {
    this.operandIndex = inputGraph.getOperandIndex(node);
    this.operandAccessor = Accessors.create(node.getOperands().get(0), inputGraph);
  }

  @Override
  public void init(ComputeMethod method) {
    this.operandAccessor.init(method);
    indexTempLocal = method.reserveLocal(1);
    rowTempLocal = method.reserveLocal(1);
    colTempLocal = method.reserveLocal(1);

  }

  @Override
  public void pushLength(ComputeMethod method) {
    operandAccessor.pushLength(method);
    MethodVisitor mv = method.getVisitor();
    mv.visitInsn(DUP);
    mv.visitInsn(IMUL);
  }

  @Override
  public boolean mustCheckForIntegerNAs() {
    return operandAccessor.mustCheckForIntegerNAs();
  }

  @Override
  public void pushElementAsDouble(ComputeMethod method, Optional<Label> integerNaLabel) {
    MethodVisitor mv = method.getVisitor();

    mv.visitInsn(DUP);
    mv.visitVarInsn(ISTORE, indexTempLocal);

    // row = index % length
    operandAccessor.pushLength(method);
    mv.visitInsn(IREM);

    mv.visitVarInsn(ISTORE, rowTempLocal);

    // col = index / length
    mv.visitVarInsn(ILOAD, indexTempLocal);
    operandAccessor.pushLength(method);
    mv.visitInsn(IDIV);
    mv.visitVarInsn(ISTORE, colTempLocal);


    // push x[row]
    mv.visitVarInsn(ILOAD, rowTempLocal);
    operandAccessor.pushElementAsDouble(method, integerNaLabel);

    // push x[col]
    mv.visitVarInsn(ILOAD, colTempLocal);
    operandAccessor.pushElementAsDouble(method, integerNaLabel);

    // x[row] - x[col]
    mv.visitInsn(DSUB);
    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "abs", "(D)D");
  }
}

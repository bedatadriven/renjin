package org.renjin.compiler.pipeline.fusion.node;

import org.renjin.compiler.pipeline.ComputeMethod;
import org.renjin.compiler.pipeline.node.DeferredNode;
import org.renjin.primitives.sequence.RepDoubleVector;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.MethodVisitor;
import org.renjin.repackaged.guava.base.Optional;

import static org.renjin.repackaged.asm.Opcodes.*;

public class RepeatingNode extends LoopNode {
  private LoopNode sourceNode;
  private LoopNode timesNode;
  private int sourceLengthLocal;

  public RepeatingNode(LoopNode sourceNode, LoopNode timesNode) {
    this.sourceNode = sourceNode;
    this.timesNode = timesNode;
  }

  public static boolean accept(DeferredNode node) {
    return node.getVector() instanceof RepDoubleVector &&
        node.getOperand(2).hasValue(1);
  }

  @Override
  public void init(ComputeMethod method) {
    MethodVisitor mv = method.getVisitor();
    sourceNode.init(method);
    timesNode.init(method);
    sourceLengthLocal = method.reserveLocal(1);
    sourceNode.pushLength(method);
    mv.visitVarInsn(ISTORE, sourceLengthLocal);
  }

  @Override
  public void pushLength(ComputeMethod method) {
    MethodVisitor mv = method.getVisitor();
    mv.visitVarInsn(ILOAD, sourceLengthLocal);
    timesNode.pushElementAsInt(method, 0);
    mv.visitInsn(IMUL);
  }

  @Override
  public boolean mustCheckForIntegerNAs() {
    return sourceNode.mustCheckForIntegerNAs();
  }

  @Override
  public void pushElementAsDouble(ComputeMethod method, Optional<Label> integerNaLabel) {
    MethodVisitor mv = method.getVisitor();
    mv.visitVarInsn(ILOAD, sourceLengthLocal);
    mv.visitInsn(IREM);
    sourceNode.pushElementAsDouble(method);
  }
}

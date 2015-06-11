package org.renjin.compiler.pipeline.accessor;

import com.google.common.base.Optional;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.renjin.compiler.pipeline.ComputeMethod;
import org.renjin.compiler.pipeline.DeferredNode;
import org.renjin.primitives.sequence.RepDoubleVector;

import static org.objectweb.asm.Opcodes.*;

public class RepeatingAccessor extends Accessor {
  private Accessor sourceAccessor;
  private Accessor timesAccessor;
  private int sourceLengthLocal;

  public RepeatingAccessor(DeferredNode node, InputGraph graph) {
    this.sourceAccessor = Accessors.create(node.getOperand(0), graph);
    this.timesAccessor = Accessors.create(node.getOperand(1), graph);
    if(!node.getOperand(2).hasValue(1)) {
      throw new IllegalArgumentException("each != 1 is not supported");
    }
  }

  public static boolean accept(DeferredNode node) {
    return node.getVector() instanceof RepDoubleVector;
  }

  @Override
  public void init(ComputeMethod method) {
    MethodVisitor mv = method.getVisitor();
    sourceAccessor.init(method);
    timesAccessor.init(method);
    sourceLengthLocal = method.reserveLocal(1);
    sourceAccessor.pushLength(method);
    mv.visitVarInsn(ISTORE, sourceLengthLocal);
  }

  @Override
  public void pushLength(ComputeMethod method) {
    MethodVisitor mv = method.getVisitor();
    mv.visitVarInsn(ILOAD, sourceLengthLocal);
    timesAccessor.pushElementAsInt(method, 0);
    mv.visitInsn(IMUL);
  }

  @Override
  public boolean mustCheckForIntegerNAs() {
    return sourceAccessor.mustCheckForIntegerNAs();
  }

  @Override
  public void pushElementAsDouble(ComputeMethod method, Optional<Label> integerNaLabel) {
    MethodVisitor mv = method.getVisitor();
    mv.visitVarInsn(ILOAD, sourceLengthLocal);
    mv.visitInsn(IREM);
    sourceAccessor.pushElementAsDouble(method);
  }
}

package org.renjin.compiler.pipeline.fusion.node;

import org.renjin.compiler.pipeline.ComputeMethod;
import org.renjin.primitives.sequence.IntSequence;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.MethodVisitor;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Optional;

import static org.renjin.repackaged.asm.Opcodes.*;

public class IntSeqNode extends LoopNode {

  private static final String SEQUENCE_CLASS = Type.getInternalName(IntSequence.class);
  
  private int operandIndex;
  private int fromVar;
  private int byVar;
  private int lengthVar;

  public IntSeqNode(int operandIndex) {
    this.operandIndex = operandIndex;
  }

  @Override
  public void init(ComputeMethod method) {
    fromVar = method.reserveLocal(1);
    byVar = method.reserveLocal(1);
    lengthVar = method.reserveLocal(1);
    
    MethodVisitor mv = method.getVisitor();
    mv.visitVarInsn(ALOAD, method.getOperandsLocalIndex());
    pushIntConstant(mv, operandIndex);
    mv.visitInsn(AALOAD);
    mv.visitTypeInsn(CHECKCAST, SEQUENCE_CLASS);

    mv.visitInsn(Opcodes.DUP);
    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SEQUENCE_CLASS, "getFrom", "()I", false);
    mv.visitVarInsn(Opcodes.ISTORE, fromVar);

    mv.visitInsn(Opcodes.DUP);
    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SEQUENCE_CLASS, "getBy", "()I", false);
    mv.visitVarInsn(Opcodes.ISTORE, byVar);

    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SEQUENCE_CLASS, "length", "()I", false);
    mv.visitVarInsn(Opcodes.ISTORE, lengthVar);
  }

  @Override
  public void pushElementAsInt(ComputeMethod method, Optional<Label> naLabel) {

    // index is on the stack already
    //  i*by + from
    MethodVisitor mv = method.getVisitor();
    mv.visitVarInsn(Opcodes.ILOAD, byVar);
    mv.visitInsn(Opcodes.IMUL);
    mv.visitVarInsn(Opcodes.ILOAD, fromVar);
    mv.visitInsn(Opcodes.IADD);
  }

  @Override
  public boolean mustCheckForIntegerNAs() {
    return false;
  }

  @Override
  public void pushElementAsDouble(ComputeMethod method, Optional<Label> integerNaLabel) {
    pushElementAsInt(method, integerNaLabel);
    method.getVisitor().visitInsn(Opcodes.I2D);
  }

  @Override
  public void pushLength(ComputeMethod method) {
    method.getVisitor().visitVarInsn(Opcodes.ILOAD, lengthVar);
  }
}

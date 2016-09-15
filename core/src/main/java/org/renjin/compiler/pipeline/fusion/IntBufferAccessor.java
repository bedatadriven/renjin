package org.renjin.compiler.pipeline.fusion;

import org.renjin.compiler.pipeline.ComputeMethod;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.MethodVisitor;
import org.renjin.repackaged.guava.base.Optional;

import static org.renjin.repackaged.asm.Opcodes.*;

/**
 * Generates the bytecode to access elements stored within an {@link java.nio.IntBuffer}
 *
 */
public class IntBufferAccessor extends Accessor {

  private int operandIndex;

  /**
   * The local variable where we're storing the
   * raw IntBuffer
   */
  private int bufferLocal;

  private int bufferLengthLocal;

  public IntBufferAccessor(int operandIndex) {
    this.operandIndex = operandIndex;
  }

  public void init(ComputeMethod method) {

    bufferLocal = method.reserveLocal(1);
    bufferLengthLocal = method.reserveLocal(1);

    MethodVisitor mv = method.getVisitor();
    mv.visitVarInsn(ALOAD, method.getOperandsLocalIndex());
    pushIntConstant(mv, operandIndex);
    mv.visitInsn(AALOAD);
    mv.visitTypeInsn(CHECKCAST, "org/renjin/sexp/IntBufferVector");
    mv.visitMethodInsn(INVOKEVIRTUAL, "org/renjin/sexp/IntBufferVector", "toIntBufferUnsafe", "()Ljava/nio/IntBuffer;", false);
    mv.visitInsn(DUP);
    mv.visitVarInsn(ASTORE, bufferLocal);
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/nio/IntBuffer", "remaining", "()I", false);
    mv.visitVarInsn(ISTORE, bufferLengthLocal);
  }

  @Override
  public void pushLength(ComputeMethod method) {
    MethodVisitor mv = method.getVisitor();
    mv.visitVarInsn(ILOAD, bufferLengthLocal);
  }

  @Override
  public void pushElementAsDouble(ComputeMethod method, Optional<Label> integerNaLabel) {
    // STACK: [ ..., index ]
    pushElementAsInt(method, integerNaLabel);
    // STACK: [ ..., ivalue ]
    MethodVisitor mv = method.getVisitor();
    mv.visitInsn(I2D);
    // STACK: [ ..., dvalue, dvalue ]
  }

  @Override
  public void pushElementAsInt(ComputeMethod method, Optional<Label> naLabel) {
    MethodVisitor mv = method.getVisitor();
    // STACK: [ ..., index]
    mv.visitVarInsn(ALOAD, bufferLocal);
    // STACK: [ ..., index, buffer ] 
    mv.visitInsn(SWAP);
    // STACK: [ ..., buffer, index ] 
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/nio/IntBuffer", "get", "(I)I", false);
    // STACK: [ ..., value ] 

    doIntegerNaCheck(mv, naLabel);
  }

  @Override
  public boolean mustCheckForIntegerNAs() {
    return true;
  }
}

package org.renjin.compiler.pipeline.accessor;

import org.objectweb.asm.MethodVisitor;
import org.renjin.compiler.pipeline.ComputeMethod;
import org.renjin.sexp.Vector;

import static org.objectweb.asm.Opcodes.*;

public class VirtualAccessor extends Accessor {

  /**
   * The local variable where we're storing the
   * raw array, double[]
   */
  private int ptrLocalIndex;
  private String vectorClass;
  private int operandIndex;

  public VirtualAccessor(Vector vector, int operandIndex) {
    this.vectorClass = vector.getClass().getName().replace('.', '/');
    this.operandIndex = operandIndex;
  }

  public void init(ComputeMethod method) {

    ptrLocalIndex = method.reserveLocal(1);

    MethodVisitor mv = method.getVisitor();
    mv.visitVarInsn(ALOAD, method.getOperandsLocalIndex());
    pushOperandIndex(mv, operandIndex);
    mv.visitInsn(AALOAD);
    mv.visitTypeInsn(CHECKCAST, vectorClass);
    mv.visitVarInsn(ASTORE, ptrLocalIndex);
  }

  @Override
  public void pushLength(ComputeMethod method) {
    MethodVisitor mv = method.getVisitor();
    mv.visitVarInsn(ALOAD, ptrLocalIndex);
    mv.visitMethodInsn(INVOKEVIRTUAL, vectorClass, "length", "()I");
  }

  @Override
  public void pushDouble(ComputeMethod method) {
    MethodVisitor mv = method.getVisitor();
    mv.visitVarInsn(ALOAD, ptrLocalIndex);
    mv.visitInsn(SWAP);
    mv.visitMethodInsn(INVOKEVIRTUAL, vectorClass, "getElementAsDouble", "(I)D");
  }

  @Override
  public void pushInt(ComputeMethod method) {
    MethodVisitor mv = method.getVisitor();
    mv.visitVarInsn(ALOAD, ptrLocalIndex);
    mv.visitInsn(SWAP);
    mv.visitMethodInsn(INVOKEVIRTUAL, vectorClass, "getElementAsInt", "(I)I");
  }
}

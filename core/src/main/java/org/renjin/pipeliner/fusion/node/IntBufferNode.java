/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.pipeliner.fusion.node;

import org.renjin.pipeliner.ComputeMethod;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.MethodVisitor;

import java.util.Optional;

import static org.renjin.repackaged.asm.Opcodes.*;

/**
 * Generates the bytecode to access elements stored within an {@link java.nio.IntBuffer}
 *
 */
public class IntBufferNode extends LoopNode {

  private int operandIndex;

  /**
   * The local variable where we're storing the
   * raw IntBuffer
   */
  private int bufferLocal;

  private int bufferLengthLocal;

  public IntBufferNode(int operandIndex) {
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

  @Override
  public void appendToKey(StringBuilder key) {
    key.append("IBN");
  }

  @Override
  public String toString() {
    return "x" + operandIndex;
  }
}

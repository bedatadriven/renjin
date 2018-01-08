/**
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
  public void appendToKey(StringBuilder key) {
    key.append("ISN");
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


  @Override
  public String toString() {
    return "x" + operandIndex;
  }
}

/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
import org.renjin.repackaged.asm.Type;

import java.util.Optional;

import static org.renjin.repackaged.asm.Opcodes.*;

public class IntArrayNode extends LoopNode {

  /**
   * The local variable where we're storing the
   * raw array, double[]
   */
  protected int arrayLocalIndex;
  protected int operandIndex;
  private String vectorType;

  public IntArrayNode(int operandIndex, Type vectorType) {
    this.operandIndex = operandIndex;
    this.vectorType = vectorType.getInternalName();
  }

  public void init(ComputeMethod method) {

    arrayLocalIndex = method.reserveLocal(1);

    MethodVisitor mv = method.getVisitor();
    mv.visitVarInsn(ALOAD, method.getOperandsLocalIndex());
    pushIntConstant(mv, operandIndex);
    mv.visitInsn(AALOAD);
    mv.visitTypeInsn(CHECKCAST, vectorType);
    mv.visitMethodInsn(INVOKEVIRTUAL, vectorType, "toIntArrayUnsafe", "()[I", false);
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

  @Override
  public void appendToKey(StringBuilder key) {
    key.append("IAN:" + vectorType);
  }

  @Override
  public String toString() {
    return "x" + operandIndex;
  }
}

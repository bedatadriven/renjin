/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
package org.renjin.compiler.pipeline.accessor;

import org.renjin.compiler.pipeline.ComputeMethod;
import org.renjin.compiler.pipeline.DeferredNode;
import org.renjin.repackaged.asm.MethodVisitor;

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
  public void pushDouble(ComputeMethod method) {
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
    operandAccessor.pushDouble(method);

    // push x[col]
    mv.visitVarInsn(ILOAD, colTempLocal);
    operandAccessor.pushDouble(method);

    // x[row] - x[col]
    mv.visitInsn(DSUB);
    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "abs", "(D)D");
  }
}

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
package org.renjin.pipeliner.fusion.kernel;

import org.renjin.pipeliner.ComputeMethod;
import org.renjin.pipeliner.fusion.node.LoopNode;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.MethodVisitor;

import static org.renjin.repackaged.asm.Opcodes.*;

public class RowMeanKernel implements LoopKernel {

  @Override
  public void compute(ComputeMethod method, LoopNode kernelOperands[]) {

    LoopNode matrix = kernelOperands[0];
    matrix.init(method);

    int meansLocal = method.reserveLocal(1);
    LoopNode numRows = kernelOperands[1];
    numRows.init(method);

    MethodVisitor mv = method.getVisitor();
    int numRowsLocal = method.reserveLocal(meansLocal);
    int rowLocal = method.reserveLocal(1);
    int counterLocal = method.reserveLocal(1);

    numRows.pushElementAsInt(method, 0);
    mv.visitInsn(DUP);
    mv.visitVarInsn(ISTORE, numRowsLocal);

    // create array (size still on stack)
    mv.visitIntInsn(NEWARRAY, T_DOUBLE);
    mv.visitVarInsn(ASTORE, meansLocal);

    // initialize counter
    mv.visitInsn(ICONST_0);
    mv.visitVarInsn(ISTORE, rowLocal);

    // initialize row index
    mv.visitInsn(ICONST_0);
    mv.visitVarInsn(ISTORE, counterLocal);

    // check whether to loop
    Label l4 = new Label();
    mv.visitLabel(l4);
    mv.visitVarInsn(ILOAD, counterLocal);
    matrix.pushLength(method);

    Label l5 = new Label();
    mv.visitJumpInsn(IF_ICMPEQ, l5);

    Label l6 = new Label();
    mv.visitLabel(l6);
    mv.visitVarInsn(ALOAD, meansLocal);
    mv.visitVarInsn(ILOAD, rowLocal);
    mv.visitInsn(DUP2);

    // load the current row sum at index rowLocal onto the stack
    mv.visitInsn(DALOAD);

    // load the next value onto the stack
    mv.visitVarInsn(ILOAD, counterLocal);
    matrix.pushElementAsDouble(method);

    // add to sum
    mv.visitInsn(DADD);
    mv.visitInsn(DASTORE);

    Label l7 = new Label();
    mv.visitLabel(l7);
    mv.visitIincInsn(rowLocal, 1);

    // check if we've hit the end of the rows
    // and need to start again
    Label l8 = new Label();
    mv.visitLabel(l8);
    mv.visitVarInsn(ILOAD, rowLocal);
    mv.visitVarInsn(ILOAD, numRowsLocal);
    Label l9 = new Label();
    mv.visitJumpInsn(IF_ICMPNE, l9);
    Label l10 = new Label();
    mv.visitLabel(l10);
    mv.visitInsn(ICONST_0);
    mv.visitVarInsn(ISTORE, rowLocal);

    // increment the vector index counter and loop
    mv.visitLabel(l9);
    mv.visitIincInsn(counterLocal, 1);
    mv.visitJumpInsn(GOTO, l4);

    mv.visitLabel(l5);

    int numColsLocal = method.reserveLocal(2);
    // calculate num cols (length / num rows)
    matrix.pushLength(method);
    mv.visitVarInsn(ILOAD, numRowsLocal);
    mv.visitInsn(IDIV);
    mv.visitInsn(I2D);
    mv.visitVarInsn(DSTORE, numColsLocal);

    // init the second loop to
    // divide out the means

    mv.visitInsn(ICONST_0);
    mv.visitVarInsn(ISTORE, counterLocal);

    // check loop
    Label l11 = new Label();
    mv.visitLabel(l11);
    mv.visitVarInsn(ILOAD, counterLocal);
    mv.visitVarInsn(ILOAD, numRowsLocal);
    Label l12 = new Label();
    mv.visitJumpInsn(IF_ICMPEQ, l12);


    Label l13 = new Label();
    mv.visitLabel(l13);
    mv.visitVarInsn(ALOAD, meansLocal);
    mv.visitVarInsn(ILOAD, counterLocal);
    mv.visitInsn(DUP2);
    // load the means[i] onto stack
    mv.visitInsn(DALOAD);
    mv.visitVarInsn(DLOAD, numColsLocal);

    mv.visitInsn(DDIV);

    // store back into means[]
    mv.visitInsn(DASTORE);
    Label l14 = new Label();

    mv.visitLabel(l14);
    mv.visitIincInsn(counterLocal, 1);
    mv.visitJumpInsn(GOTO, l11);
    mv.visitLabel(l12);

    mv.visitVarInsn(ALOAD, meansLocal);
    mv.visitInsn(ARETURN);
  }

  @Override
  public String debugLabel(LoopNode[] operands) {
    return "rowMeans(" + operands[0] + ")";
  }

  @Override
  public void appendToKey(StringBuilder key) {
    key.append("rowMeans");
  }
}

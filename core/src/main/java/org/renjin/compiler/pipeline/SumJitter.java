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
package org.renjin.compiler.pipeline;


import org.renjin.compiler.pipeline.accessor.Accessor;
import org.renjin.compiler.pipeline.accessor.Accessors;
import org.renjin.compiler.pipeline.accessor.InputGraph;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.MethodVisitor;

import static org.renjin.repackaged.asm.Opcodes.*;

public class SumJitter implements FunctionJitter {
  @Override
  public void compute(ComputeMethod method, DeferredNode node) {

    InputGraph inputGraph = new InputGraph(node);

    Accessor accessor = Accessors.create(node.getOperands().get(0), inputGraph);
    accessor.init(method);

    MethodVisitor mv = method.getVisitor();

    // get the length of the vector
    int lengthLocal = method.reserveLocal(1);
    accessor.pushLength(method);
    mv.visitVarInsn(ISTORE, lengthLocal);

    // initial the sum variable
    int sumLocal = method.reserveLocal(2);
    mv.visitInsn(DCONST_0);
    mv.visitVarInsn(DSTORE, sumLocal);

    int counterLocal = method.reserveLocal(1);
    mv.visitInsn(ICONST_0);
    mv.visitVarInsn(ISTORE, counterLocal);

    Label l3 = new Label();
    mv.visitLabel(l3);
    mv.visitVarInsn(ILOAD, counterLocal);
    mv.visitVarInsn(ILOAD, lengthLocal);

    Label l4 = new Label();
    mv.visitJumpInsn(IF_ICMPEQ, l4);

    Label l5 = new Label();
    mv.visitLabel(l5);

    // load the sum on to the stack, and the next value
    mv.visitVarInsn(DLOAD, sumLocal);
    mv.visitVarInsn(ILOAD, counterLocal);
    accessor.pushDouble(method);

    // add the two values and store back into sum
    mv.visitInsn(DADD);
    mv.visitVarInsn(DSTORE, sumLocal);

    Label l6 = new Label();
    mv.visitLabel(l6);
    mv.visitIincInsn(counterLocal, 1);
    mv.visitJumpInsn(GOTO, l3);
    mv.visitLabel(l4);

    // return result
    mv.visitInsn(ICONST_1);
    mv.visitIntInsn(NEWARRAY, T_DOUBLE);
    mv.visitInsn(DUP);
    mv.visitInsn(ICONST_0);
    mv.visitVarInsn(DLOAD, sumLocal);
    mv.visitInsn(DASTORE);
    mv.visitInsn(ARETURN);
  }
}

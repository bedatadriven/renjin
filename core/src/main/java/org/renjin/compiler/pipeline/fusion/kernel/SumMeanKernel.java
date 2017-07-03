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
package org.renjin.compiler.pipeline.fusion.kernel;

import org.renjin.compiler.pipeline.ComputeMethod;
import org.renjin.compiler.pipeline.fusion.node.LoopNode;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.MethodVisitor;

import static org.renjin.repackaged.asm.Opcodes.*;

public class SumMeanKernel implements LoopKernel {

  private boolean mean;

  private SumMeanKernel(boolean mean) {
    this.mean = mean;
  }

  public static SumMeanKernel mean() {
    return new SumMeanKernel(true);
  } 
  
  public static SumMeanKernel sum() {
    return new SumMeanKernel(false);
  }
  
  @Override
  public void compute(ComputeMethod method, LoopNode[] operands) {

    
    MethodVisitor mv = method.getVisitor();

    LoopNode vector = operands[0];
    vector.init(method);

    // get the length of the vector
    int lengthLocal = method.reserveLocal(1);
    vector.pushLength(method);
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
    vector.pushElementAsDouble(method);

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
    
    if(mean) {
      mv.visitVarInsn(ILOAD, lengthLocal);
      mv.visitInsn(I2D);
      mv.visitInsn(DDIV);
    }
    
    mv.visitInsn(DASTORE);
    mv.visitInsn(ARETURN);
  }

  @Override
  public String debugLabel(LoopNode[] operands) {
    return (mean ? "mean" : "sum") + "(" + operands[0] + ")";
  }
}

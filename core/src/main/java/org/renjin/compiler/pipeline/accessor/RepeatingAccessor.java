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
import org.renjin.primitives.sequence.RepDoubleVector;
import org.renjin.repackaged.asm.MethodVisitor;

import static org.renjin.repackaged.asm.Opcodes.*;

public class RepeatingAccessor extends Accessor {
  private Accessor sourceAccessor;
  private Accessor timesAccessor;
  private int sourceLengthLocal;

  public RepeatingAccessor(DeferredNode node, InputGraph graph) {
    this.sourceAccessor = Accessors.create(node.getOperand(0), graph);
    this.timesAccessor = Accessors.create(node.getOperand(1), graph);
    if(!node.getOperand(2).hasValue(1)) {
      throw new IllegalArgumentException("each != 1 is not supported");
    }
  }

  public static boolean accept(DeferredNode node) {
    return node.getVector() instanceof RepDoubleVector;
  }

  @Override
  public void init(ComputeMethod method) {
    MethodVisitor mv = method.getVisitor();
    sourceAccessor.init(method);
    timesAccessor.init(method);
    sourceLengthLocal = method.reserveLocal(1);
    sourceAccessor.pushLength(method);
    mv.visitVarInsn(ISTORE, sourceLengthLocal);
  }

  @Override
  public void pushLength(ComputeMethod method) {
    MethodVisitor mv = method.getVisitor();
    mv.visitVarInsn(ILOAD, sourceLengthLocal);
    mv.visitInsn(ICONST_0);
    timesAccessor.pushInt(method);
    mv.visitInsn(IMUL);
  }

  @Override
  public void pushDouble(ComputeMethod method) {
    MethodVisitor mv = method.getVisitor();
    mv.visitVarInsn(ILOAD, sourceLengthLocal);
    mv.visitInsn(IREM);
    sourceAccessor.pushDouble(method);
  }
}

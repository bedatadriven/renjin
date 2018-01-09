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
import org.renjin.pipeliner.node.DeferredNode;
import org.renjin.primitives.sequence.RepDoubleVector;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.MethodVisitor;

import java.util.Optional;

import static org.renjin.repackaged.asm.Opcodes.*;

public class RepeatingNode extends LoopNode {
  private LoopNode sourceNode;
  private LoopNode timesNode;
  private int sourceLengthLocal;

  public RepeatingNode(LoopNode sourceNode, LoopNode timesNode) {
    this.sourceNode = sourceNode;
    this.timesNode = timesNode;
  }

  public static boolean accept(DeferredNode node) {
    return node.getVector() instanceof RepDoubleVector &&
        node.getOperand(2).hasValue(1);
  }

  @Override
  public void init(ComputeMethod method) {
    MethodVisitor mv = method.getVisitor();
    sourceNode.init(method);
    timesNode.init(method);
    sourceLengthLocal = method.reserveLocal(1);
    sourceNode.pushLength(method);
    mv.visitVarInsn(ISTORE, sourceLengthLocal);
  }

  @Override
  public void pushLength(ComputeMethod method) {
    MethodVisitor mv = method.getVisitor();
    mv.visitVarInsn(ILOAD, sourceLengthLocal);
    timesNode.pushElementAsInt(method, 0);
    mv.visitInsn(IMUL);
  }

  @Override
  public boolean mustCheckForIntegerNAs() {
    return sourceNode.mustCheckForIntegerNAs();
  }

  @Override
  public void appendToKey(StringBuilder key) {
    key.append("rep(");
    sourceNode.appendToKey(key);
    key.append(',');
    timesNode.appendToKey(key);
    key.append(')');
  }

  @Override
  public void pushElementAsDouble(ComputeMethod method, Optional<Label> integerNaLabel) {
    MethodVisitor mv = method.getVisitor();
    mv.visitVarInsn(ILOAD, sourceLengthLocal);
    mv.visitInsn(IREM);
    sourceNode.pushElementAsDouble(method);
  }

  @Override
  public String toString() {
    return "rep(" + sourceNode + ", " + timesNode + ")";
  }
}

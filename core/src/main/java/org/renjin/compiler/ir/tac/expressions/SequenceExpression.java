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
package org.renjin.compiler.ir.tac.expressions;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.primitives.sequence.DoubleSequence;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.AtomicVector;

import java.util.Map;

public class SequenceExpression extends SpecializedCallExpression {
 
  private ValueBounds valueBounds;

  public SequenceExpression(Expression from, Expression to) {
    super(from, to);
  }

  @Override
  public boolean isFunctionDefinitelyPure() {
    return true;
  }

  @Override
  public boolean isDefinitelyPure() {
    return true;
  }

  @Override
  public ValueBounds updateTypeBounds(Map<Expression, ValueBounds> typeMap) {
    ValueBounds fromType = childAt(0).updateTypeBounds(typeMap);
    ValueBounds toType = childAt(1).updateTypeBounds(typeMap);

    valueBounds = ValueBounds.vector(fromType.getTypeSet() | toType.getTypeSet());
    
    return valueBounds;
  }

  @Override
  public ValueBounds getValueBounds() {
    return valueBounds;
  }

  @Override
  public int load(EmitContext emitContext, InstructionAdapter mv) {
    int stackSizeIncrease =
        assertDouble(childAt(0)).load(emitContext, mv) + 
        assertDouble(childAt(1)).load(emitContext, mv);

    mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(DoubleSequence.class), "fromTo",
        Type.getMethodDescriptor(Type.getType(AtomicVector.class), Type.DOUBLE_TYPE, Type.DOUBLE_TYPE), false);

    return stackSizeIncrease;
  }

  @Override
  public Type getType() {
    return valueBounds.storageType();
  }

  private Expression assertDouble(Expression expression) {
//    if(!expression.getType().equals(double.class)) {
//      throw new AssertionError(expression + " has a type of " + expression.getType() + " expected double");
//    }
//    return expression;
    throw new UnsupportedOperationException();
  }

  
  
  
  @Override
  public String toString() {
    return childAt(0) + ":" + childAt(1);
  }
}

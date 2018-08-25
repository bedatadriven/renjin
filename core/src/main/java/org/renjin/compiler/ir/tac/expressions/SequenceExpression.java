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
package org.renjin.compiler.ir.tac.expressions;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.primitives.sequence.DoubleSequence;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.DoubleVector;

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
  public boolean isPure() {
    return true;
  }

  @Override
  public ValueBounds updateTypeBounds(Map<Expression, ValueBounds> typeMap) {
    int fromType = endpointType(childAt(0).updateTypeBounds(typeMap));
    int toType = endpointType(childAt(1).updateTypeBounds(typeMap));

    valueBounds = ValueBounds.builder()
        .setTypeSet(fromType | toType)
        .setNA(ValueBounds.NO_NA)
        .setEmptyAttributes()
        .build();

    return valueBounds;
  }

  private int endpointType(ValueBounds valueBounds) {
    // Should we treat this is a integer, even though it might be of type double?
    if(valueBounds.isConstant() && valueBounds.getConstantValue() instanceof DoubleVector) {
      double doubleValue = ((DoubleVector) valueBounds.getConstantValue()).getElementAsDouble(0);
      if(doubleValue < Integer.MAX_VALUE && doubleValue == (int)doubleValue) {
        return TypeSet.INT;
      }
    }
    return valueBounds.getTypeSet();
  }

  @Override
  public ValueBounds getValueBounds() {
    return valueBounds;
  }

  @Override
  public int load(EmitContext emitContext, InstructionAdapter mv) {

    childAt(0).load(emitContext, mv);
    emitContext.convert(mv, childAt(0).getType(), Type.DOUBLE_TYPE);

    childAt(1).load(emitContext, mv);
    emitContext.convert(mv, childAt(1).getType(), Type.DOUBLE_TYPE);

    mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(DoubleSequence.class), "fromTo",
        Type.getMethodDescriptor(Type.getType(AtomicVector.class), Type.DOUBLE_TYPE, Type.DOUBLE_TYPE), false);

    return 0;
  }

  @Override
  public Type getType() {
    return valueBounds.storageType();
  }


  @Override
  public String toString() {
    return childAt(0) + ":" + childAt(1);
  }
}

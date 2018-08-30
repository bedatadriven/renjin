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
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.VectorType;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.primitives.sequence.Sequences;
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
    ValueBounds fromBounds = childAt(0).updateTypeBounds(typeMap);
    ValueBounds toBounds = childAt(1).updateTypeBounds(typeMap);

    int fromType = endpointType(fromBounds);
    int toType = endpointType(toBounds);

    this.valueBounds = ValueBounds.builder()
        .setTypeSet(fromType | toType)
        .setFlag(ValueBounds.FLAG_NO_NA)
        .setFlag(ValueBounds.FLAG_POSITIVE,
            toBounds.isFlagSet(ValueBounds.FLAG_POSITIVE) &&
            toBounds.isFlagSet(ValueBounds.FLAG_POSITIVE))
        .setEmptyAttributes()
        .build();

    return this.valueBounds;
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
  public CompiledSexp getCompiledExpr(EmitContext emitContext) {
    CompiledSexp fromExpr = childAt(0).getCompiledExpr(emitContext);
    CompiledSexp toExpr = childAt(1).getCompiledExpr(emitContext);

    return new CompiledSexp() {
      @Override
      public void loadSexp(EmitContext context, InstructionAdapter mv) {
        fromExpr.loadScalar(emitContext, mv, VectorType.DOUBLE);
        toExpr.loadScalar(emitContext, mv, VectorType.DOUBLE);
        mv.invokestatic(Type.getInternalName(Sequences.class), "colonSequence",
            Type.getMethodDescriptor(Type.getType(AtomicVector.class), Type.DOUBLE_TYPE, Type.DOUBLE_TYPE), false);
      }

      @Override
      public void loadScalar(EmitContext context, InstructionAdapter mv, VectorType vectorType) {
        fromExpr.loadScalar(emitContext, mv, vectorType);
      }

      @Override
      public void loadArray(EmitContext context, InstructionAdapter mv, VectorType vectorType) {
        switch (vectorType) {
          case INT:
            fromExpr.loadScalar(context, mv, VectorType.INT);
            toExpr.loadScalar(context, mv, VectorType.INT);
            mv.invokestatic(Type.getInternalName(Sequences.class), "colonSequence", "(II)[I", false);
            break;
          default:
            throw new UnsupportedOperationException("TODO: " + vectorType);
        }
      }

      @Override
      public void loadLength(EmitContext context, InstructionAdapter mv) {
        throw new UnsupportedOperationException("TODO");
      }

      @Override
      public CompiledSexp elementAt(EmitContext context, CompiledSexp indexExpr) {
        throw new UnsupportedOperationException("TODO");
      }
    };
  }


  @Override
  public String toString() {
    return childAt(0) + ":" + childAt(1);
  }
}

/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */

package org.renjin.gcc.codegen.type.primitive;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.condition.Comparison;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.type.NumericExpr;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.type.GimpleRealType;
import org.renjin.gcc.runtime.LongPtr;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nullable;

public class UnsignedLongExpr extends AbstractIntExpr {

  public UnsignedLongExpr(JExpr jexpr, @Nullable GExpr address) {
    super(jexpr, address);
  }

  public UnsignedLongExpr(JExpr jExpr) {
    this(jExpr, null);
  }

  private UnsignedLongExpr lift(JExpr jExpr) {
    assert jExpr.getType().equals(Type.LONG_TYPE);
    return new UnsignedLongExpr(jExpr);
  }

  private JExpr jexpr(GExpr operand) {
    return operand.toPrimitiveExpr().toUnsignedInt(64).jexpr();
  }

  @Override
  public NumericExpr plus(GExpr operand) {
    return lift(Expressions.sum(jexpr(), jexpr(operand)));
  }

  @Override
  public NumericExpr minus(GExpr operand) {
    return lift(Expressions.difference(jexpr(), jexpr(operand)));
  }

  @Override
  public NumericExpr multiply(GExpr operand) {
    return lift(Expressions.product(jexpr(), jexpr(operand)));
  }

  @Override
  public NumericExpr divide(GExpr operand) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public NumericExpr negative() {
    throw new UnsupportedOperationException("unsigned values cannot be negative.");
  }

  @Override
  public NumericExpr min(GExpr operand) {
    return lift(Expressions.staticMethodCall(LongPtr.class, "unsignedMin", "(JJ)J", jexpr(), jexpr(operand)));
  }

  @Override
  public NumericExpr max(GExpr operand) {
    return lift(Expressions.staticMethodCall(LongPtr.class, "unsignedMax", "(JJ)J", jexpr(), jexpr(operand)));
  }

  @Override
  public NumericExpr absoluteValue() {
    return this;
  }

  @Override
  public GExpr remainder(GExpr operand) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public GExpr bitwiseExclusiveOr(GExpr operand) {
    return lift(Expressions.bitwiseXor(jexpr(), jexpr(operand)));
  }

  @Override
  public GExpr bitwiseNot() {
    return lift(Expressions.bitwiseXor(jexpr(), Expressions.constantLong(-1L)));
  }

  @Override
  public GExpr bitwiseAnd(GExpr operand) {
    return lift(Expressions.bitwiseAnd(jexpr(), jexpr(operand)));
  }

  @Override
  public GExpr bitwiseOr(GExpr operand) {
    return lift(Expressions.bitwiseOr(jexpr(), jexpr(operand)));
  }

  @Override
  public GExpr shiftLeft(GExpr operand) {
    return lift(Expressions.shiftLeft(jexpr(), jexpr(operand)));
  }

  @Override
  public GExpr shiftRight(GExpr operand) {
    return lift(Expressions.unsignedShiftRight(jexpr(), jexpr(operand)));
  }

  @Override
  public GExpr rotateLeft(GExpr operand) {
    return lift(Expressions.staticMethodCall(Long.class, "rotateLeft", "(JI)J", jexpr(), bits(operand)));
  }

  @Override
  public IntExpr toSignedInt(int precision) {
    switch (precision) {
      case 64:
      return new SignedLongExpr(jexpr());
      case 32:
      case 16:
      case 8:
        return new SignedIntExpr(Expressions.l2i(jexpr())).toSignedInt(precision);
    }
    throw new UnsupportedOperationException("precision: " + precision);
  }

  @Override
  public IntExpr toUnsignedInt(int precision) {
    switch (precision) {
      case 64:
        return this;
      case 32:
      case 16:
      case 8:
        return new UnsignedIntExpr(Expressions.l2i(jexpr())).toUnsignedInt(precision);
    }
    throw new UnsupportedOperationException("precision: " + precision);
  }


  @Override
  public RealExpr toRealExpr() {
    return new RealExpr(new GimpleRealType(64),
        Expressions.staticMethodCall(LongPtr.class, "unsignedInt64ToReal64", "(J)D", jexpr()));
  }

  @Override
  public RealExpr toReal(int precision) {
    return toRealExpr().toReal(precision);
  }

  @Override
  public ConditionGenerator compareTo(GimpleOp op, GExpr operand) {
    switch (op) {
      case EQ_EXPR:
      case NE_EXPR:
        return new Comparison(op, Expressions.lcmp(jexpr(), jexpr(operand)));
      default:
        throw new UnsupportedOperationException("TODO");
    }
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {
    ((JLValue) jexpr()).store(mv, jexpr(rhs));
  }
}

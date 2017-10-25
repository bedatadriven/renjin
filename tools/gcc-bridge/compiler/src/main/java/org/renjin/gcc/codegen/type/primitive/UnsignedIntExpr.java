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
package org.renjin.gcc.codegen.type.primitive;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.condition.Comparison;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.condition.IntegerComparison;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.type.NumericExpr;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.runtime.IntPtr;

import javax.annotation.Nullable;

import static org.renjin.gcc.codegen.expr.Expressions.constantLong;
import static org.renjin.gcc.codegen.expr.Expressions.i2l;

/**
 * 32-bit unsigned integer
 */
public class UnsignedIntExpr extends AbstractIntExpr {

  public UnsignedIntExpr(JExpr jexpr, @Nullable GExpr address) {
    super(jexpr, address);
  }

  public UnsignedIntExpr(JExpr jexpr) {
    this(jexpr, null);
  }

  private UnsignedIntExpr lift(JExpr expr) {
    return new UnsignedIntExpr(expr);
  }

  @Override
  public NumericExpr plus(GExpr operand) {
    return lift(Expressions.sum(jexpr(), jexpr(operand)));
  }

  private JExpr jexpr(GExpr operand) {
    return operand.toPrimitiveExpr().toUnsignedInt(32).jexpr();
  }

  private JExpr jlongExpr() {
    JExpr longValue = i2l(jexpr());
    return Expressions.bitwiseAnd(longValue, constantLong(0xffffffffL));
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
    return lift(Expressions.staticMethodCall(IntPtr.class, "unsignedDivide", "(II)I", jexpr(), jexpr(operand)));
  }

  @Override
  public NumericExpr negative() {
    // Negative operation on an unsigned int doesn't make any sense to me,
    // but it does occur in Gimple output and seems to work if we use the signed negative operator.
    return lift(Expressions.negative(jexpr()));
  }

  @Override
  public NumericExpr min(GExpr operand) {
    return lift(Expressions.staticMethodCall(IntPtr.class, "unsignedMin", "(II)I", jexpr(), jexpr(operand)));
  }

  @Override
  public NumericExpr max(GExpr operand) {
    return lift(Expressions.staticMethodCall(IntPtr.class, "unsignedMax", "(II)I", jexpr(), jexpr(operand)));
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
    return lift(Expressions.bitwiseXor(jexpr(), -1));
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
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public RealExpr toRealExpr() {
    return toReal(32);
  }

  @Override
  public IntExpr toSignedInt(int precision) {
    switch (precision) {
      case 8:
        return new SignedIntExpr(Expressions.i2b(jexpr()));
      case 16:
        return new SignedIntExpr(Expressions.i2s(jexpr()));
      case 32:
        return this;
      case 64:
        return new SignedLongExpr(jlongExpr());
    }
    throw new UnsupportedOperationException("32 => " + precision);
  }

  @Override
  public IntExpr toUnsignedInt(int precision) {
    switch (precision) {
      case 8:
        return new UnsignedSmallIntExpr(8, Expressions.bitwiseAnd(jexpr(), 0xFF));
      case 16:
        return new UnsignedSmallIntExpr(16, Expressions.i2c(jexpr()));
      case 32:
        return this;
      case 64:
        return new UnsignedLongExpr(jlongExpr());
    }
    throw new IllegalArgumentException("precision: " + precision);
  }

  @Override
  public RealExpr toReal(int precision) {
    return toSignedInt(64).toReal(precision);
  }

  @Override
  public ConditionGenerator compareTo(GimpleOp op, GExpr operand) {
    switch (op) {
      case EQ_EXPR:
      case NE_EXPR:
        return new IntegerComparison(op, jexpr(), jexpr(operand));

      default:
        return new Comparison(op, Expressions.staticMethodCall(IntPtr.class, "unsignedCompare", "(II)I",
            jexpr(), jexpr(operand)));
    }
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {
    ((JLValue) jexpr()).store(mv, jexpr(rhs));
  }
}

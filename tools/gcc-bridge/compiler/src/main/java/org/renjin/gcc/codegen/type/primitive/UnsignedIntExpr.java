/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.runtime.IntPtr;

import javax.annotation.Nullable;

/**
 * 32-bit unsigned integer
 */
public class UnsignedIntExpr extends AbstractIntExpr {

  public UnsignedIntExpr(JExpr jexpr, @Nullable PtrExpr address) {
    super(jexpr, address);
  }

  public UnsignedIntExpr(JExpr jexpr) {
    this(jexpr, null);
  }

  private UnsignedIntExpr lift(JExpr expr) {
    return new UnsignedIntExpr(expr);
  }

  @Override
  public UnsignedIntExpr plus(GExpr operand) {
    return lift(Expressions.sum(jexpr(), jexpr(operand)));
  }

  private JExpr jexpr(GExpr operand) {
    return operand.toPrimitiveExpr().toUnsignedInt(32).jexpr();
  }

  private JExpr jlongExpr() {
    return Expressions.staticMethodCall(Integer.class, "toUnsignedLong", "(I)J", jexpr());
  }

  @Override
  public UnsignedIntExpr minus(GExpr operand) {
    return lift(Expressions.difference(jexpr(), jexpr(operand)));
  }

  @Override
  public UnsignedIntExpr multiply(GExpr operand) {
    return lift(Expressions.product(jexpr(), jexpr(operand)));
  }

  @Override
  public UnsignedIntExpr divide(GExpr operand) {
    return lift(Expressions.staticMethodCall(Integer.class, "divideUnsigned", "(II)I", jexpr(), jexpr(operand)));
  }

  @Override
  public UnsignedIntExpr negative() {
    // Negative operation on an unsigned int doesn't make any sense to me,
    // but it does occur in Gimple output and seems to work if we use the signed negative operator.
    return lift(Expressions.negative(jexpr()));
  }

  @Override
  public UnsignedIntExpr min(GExpr operand) {
    return lift(Expressions.staticMethodCall(IntPtr.class, "unsignedMin", "(II)I", jexpr(), jexpr(operand)));
  }

  @Override
  public UnsignedIntExpr max(GExpr operand) {
    return lift(Expressions.staticMethodCall(IntPtr.class, "unsignedMax", "(II)I", jexpr(), jexpr(operand)));
  }

  @Override
  public UnsignedIntExpr absoluteValue() {
    return this;
  }

  @Override
  public UnsignedIntExpr remainder(GExpr operand) {
    return lift(Expressions.staticMethodCall(Integer.class, "remainderUnsigned", "(II)I", jexpr(), jexpr(operand)));
  }

  @Override
  public UnsignedIntExpr bitwiseXor(GExpr operand) {
    return lift(Expressions.bitwiseXor(jexpr(), jexpr(operand)));
  }

  @Override
  public UnsignedIntExpr bitwiseNot() {
    return lift(Expressions.bitwiseXor(jexpr(), -1));
  }

  @Override
  public UnsignedIntExpr bitwiseAnd(GExpr operand) {
    return lift(Expressions.bitwiseAnd(jexpr(), jexpr(operand)));
  }

  @Override
  public UnsignedIntExpr bitwiseOr(GExpr operand) {
    return lift(Expressions.bitwiseOr(jexpr(), jexpr(operand)));
  }

  @Override
  public UnsignedIntExpr shiftLeft(GExpr operand) {
    return lift(Expressions.shiftLeft(jexpr(), jexpr(operand)));
  }

  @Override
  public UnsignedIntExpr shiftRight(GExpr operand) {
    return lift(Expressions.unsignedShiftRight(jexpr(), jexpr(operand)));
  }

  @Override
  public UnsignedIntExpr rotateLeft(GExpr operand) {
    return lift(Expressions.staticMethodCall(Integer.class, "rotateLeft", "(II)I", jexpr(), bits(operand)));
  }

  @Override
  public GimplePrimitiveType getType() {
    return GimpleIntegerType.unsigned(32);
  }

  @Override
  public RealExpr toRealExpr() {
    return toReal(32);
  }

  @Override
  public BooleanExpr toBooleanExpr() {
    return BooleanExpr.fromInt(jexpr());
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
        return new Comparison(op, Expressions.staticMethodCall(Integer.class, "compareUnsigned", "(II)I",
            jexpr(), jexpr(operand)));
    }
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {
    ((JLValue) jexpr()).store(mv, jexpr(rhs));
  }
}

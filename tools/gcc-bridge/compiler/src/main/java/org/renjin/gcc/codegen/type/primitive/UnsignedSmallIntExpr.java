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
package org.renjin.gcc.codegen.type.primitive;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.condition.IntegerComparison;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.type.NumericExpr;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleRealType;

/**
 * Unsigned 8-bit and 16-bit values.
 *
 */
public class UnsignedSmallIntExpr extends AbstractIntExpr {

  private final int precision;

  public UnsignedSmallIntExpr(int precision, JExpr expr, PtrExpr address) {
    super(expr, address);
    this.precision = precision;
  }

  public UnsignedSmallIntExpr(int precision, JExpr expr) {
    this(precision, expr, null);
  }

  private UnsignedSmallIntExpr lift(JExpr expr) {
    return new UnsignedSmallIntExpr(this.precision, expr);
  }

  private JExpr truncate(JExpr expr) {
    switch (precision) {
      case 8:
        return Expressions.bitwiseAnd(expr, 0xFF);
      case 16:
        return Expressions.i2c(expr);
      default:
        return expr;
    }
  }

  @Override
  public NumericExpr plus(GExpr operand) {
    return lift(truncate(Expressions.sum(jexpr(), jexpr(operand))));
  }
  @Override
  public NumericExpr minus(GExpr operand) {
    return lift(truncate(Expressions.sum(jexpr(), jexpr(operand))));
  }

  @Override
  public NumericExpr multiply(GExpr operand) {
    return lift(truncate(Expressions.sum(jexpr(), jexpr(operand))));
  }

  @Override
  public NumericExpr divide(GExpr operand) {
    // Do we need to truncate here?
    return lift(truncate(Expressions.divide(jexpr(), jexpr(operand))));
  }

  @Override
  public NumericExpr negative() {
    return lift(truncate(Expressions.negative(jexpr())));
  }

  @Override
  public UnsignedSmallIntExpr min(GExpr operand) {
    // No need to truncate - result will be in range if both arguments are in range
    return lift(Expressions.staticMethodCall(Math.class, "min", "(II)I", jexpr(), jexpr(operand)));
  }

  @Override
  public UnsignedSmallIntExpr max(GExpr operand) {
    // No need to truncate - result will be in range if both arguments are in range
    return lift(Expressions.staticMethodCall(Math.class, "max", "(II)I", jexpr(), jexpr(operand)));
  }

  @Override
  public UnsignedSmallIntExpr absoluteValue() {
    return this;
  }

  @Override
  public UnsignedSmallIntExpr remainder(GExpr operand) {
    return lift(truncate(Expressions.remainder(jexpr(), jexpr(operand))));
  }

  @Override
  public UnsignedSmallIntExpr bitwiseXor(GExpr operand) {
    // No truncation needed
    return lift(Expressions.bitwiseXor(jexpr(), jexpr(operand)));
  }

  @Override
  public UnsignedSmallIntExpr bitwiseNot() {
    switch (precision) {
      case 8:
        return lift(Expressions.bitwiseXor(jexpr(), 0xFF));
      case 16:
        return lift(Expressions.bitwiseXor(jexpr(), 0xFFFFF));
    }
    throw new IllegalStateException();
  }

  @Override
  public UnsignedSmallIntExpr bitwiseAnd(GExpr operand) {
    return lift(Expressions.bitwiseAnd(jexpr(), jexpr(operand)));
  }

  @Override
  public UnsignedSmallIntExpr bitwiseOr(GExpr operand) {
    return lift(Expressions.bitwiseOr(jexpr(), jexpr(operand)));
  }

  @Override
  public UnsignedSmallIntExpr shiftLeft(GExpr operand) {
    return lift(truncate(Expressions.shiftLeft(jexpr(), operand.toPrimitiveExpr().toSignedInt(32).jexpr())));
  }

  @Override
  public UnsignedSmallIntExpr shiftRight(GExpr operand) {
    return lift(truncate(Expressions.shiftRight(jexpr(), operand.toPrimitiveExpr().toSignedInt(32).jexpr())));
  }

  @Override
  public UnsignedSmallIntExpr rotateLeft(GExpr operand) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public GimplePrimitiveType getType() {
    return GimpleIntegerType.unsigned(precision);
  }

  @Override
  public RealExpr toRealExpr() {
    return toReal(precision);
  }

  @Override
  public BooleanExpr toBooleanExpr() {
    return BooleanExpr.fromInt(jexpr());
  }

  @Override
  public IntExpr toSignedInt(int precision) {
    if(precision == 64) {
      return new SignedLongExpr(Expressions.i2l(jexpr()));
    } else {
      return new SignedIntExpr(jexpr()).toSignedInt(precision);
    }
  }

  @Override
  public IntExpr toUnsignedInt(int precision) {
    if(this.precision == precision) {
      return this;
    } else {
      return new UnsignedIntExpr(jexpr()).toUnsignedInt(precision);
    }
  }

  @Override
  public RealExpr toReal(int precision) {
    switch (precision) {
      case 32:
        return new RealExpr(new GimpleRealType(32), Expressions.i2f(jexpr()));
      case 64:
      case 96:
        return new RealExpr(new GimpleRealType(precision), Expressions.i2d(jexpr()));
    }
    throw new UnsupportedOperationException("precision: " + precision);
  }

  @Override
  public ConditionGenerator compareTo(GimpleOp op, GExpr operand) {
    // We can use normal "signed" comparison because the entire range of an unsigned 8-bit or 16-bit
    // integer will have the same positive sign as a 32-bit signed integer encoded as two's-complement.
    return new IntegerComparison(op, jexpr(), jexpr(operand));
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {
    ((JLValue) jexpr()).store(mv, jexpr(rhs));
  }

  private JExpr jexpr(GExpr operand) {
    return operand.toPrimitiveExpr().toUnsignedInt(precision).jexpr();
  }
}

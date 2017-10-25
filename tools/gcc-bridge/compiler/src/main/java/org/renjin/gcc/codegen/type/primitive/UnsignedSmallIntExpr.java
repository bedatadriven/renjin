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
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.condition.IntegerComparison;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.type.NumericExpr;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.type.GimpleRealType;

/**
 * Unsigned 8-bit and 16-bit values.
 *
 * <p>The JVM stores 8-bit and 16-bit values as 32-bit integers on the stack, so there's no
 * reason to not just pretend that they are integers.</p>
 */
public class UnsignedSmallIntExpr extends AbstractIntExpr {

  private final int precision;

  public UnsignedSmallIntExpr(int precision, JExpr expr, GExpr address) {
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
    throw new UnsupportedOperationException("Negative value of unsigned integer??");
  }
  @Override
  public IntExpr min(GExpr operand) {
    // No need to truncate - result will be in range if both arguments are in range
    return lift(Expressions.staticMethodCall(Math.class, "min", "(II)I"));
  }

  @Override
  public IntExpr max(GExpr operand) {
    // No need to truncate - result will be in range if both arguments are in range
    return lift(Expressions.staticMethodCall(Math.class, "max", "(II)I"));
  }

  @Override
  public IntExpr absoluteValue() {
    return this;
  }

  @Override
  public GExpr remainder(GExpr operand) {
    return lift(truncate(Expressions.remainder(jexpr(), jexpr(operand))));
  }

  @Override
  public GExpr bitwiseExclusiveOr(GExpr operand) {
    // No truncation needed
    return lift(Expressions.bitwiseXor(jexpr(), jexpr(operand)));
  }

  @Override
  public GExpr bitwiseNot() {
    switch (precision) {
      case 8:
        return lift(Expressions.bitwiseXor(jexpr(), 0xFF));
      case 16:
        return lift(Expressions.bitwiseXor(jexpr(), 0xFFFFF));
    }
    throw new IllegalStateException();
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
    return lift(truncate(Expressions.shiftLeft(jexpr(), operand.toPrimitiveExpr().toSignedInt(32).jexpr())));
  }

  @Override
  public GExpr shiftRight(GExpr operand) {
    return lift(truncate(Expressions.shiftRight(jexpr(), operand.toPrimitiveExpr().toSignedInt(32).jexpr())));
  }

  @Override
  public GExpr rotateLeft(GExpr operand) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public RealExpr toRealExpr() {
    return toReal(precision);
  }

  @Override
  public IntExpr toSignedInt(int precision) {
    if(precision == 64) {
      return new SignedLongExpr(Expressions.i2l(jexpr()));
    } else if(this.precision <= precision) {
      // widening to int32
      return new SignedIntExpr(jexpr());
    }
    throw new UnsupportedOperationException("unsigned" + this.precision + " => " + "signed" + precision);
  }

  @Override
  public IntExpr toUnsignedInt(int precision) {
    if(precision < 32) {
      return new UnsignedSmallIntExpr(precision, jexpr());
    } else if(precision == 32) {
      return new UnsignedIntExpr(jexpr());
    }
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public RealExpr toReal(int precision) {
    switch (precision) {
      case 32:
        return new RealExpr(new GimpleRealType(32), Expressions.i2f(jexpr()));
      case 64:
        return new RealExpr(new GimpleRealType(64), Expressions.i2d(jexpr()));
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

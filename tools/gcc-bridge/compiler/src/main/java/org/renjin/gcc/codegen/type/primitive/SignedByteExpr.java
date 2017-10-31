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
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.condition.IntegerComparison;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleRealType;

import javax.annotation.Nullable;

import static org.renjin.gcc.codegen.expr.Expressions.i2b;
import static org.renjin.gcc.codegen.expr.Expressions.i2l;

/**
 * Signed 8-bit value
 */
public class SignedByteExpr extends AbstractIntExpr implements IntExpr {

  public SignedByteExpr(JExpr expr, @Nullable PtrExpr address) {
    super(expr, address);
  }

  public SignedByteExpr(JExpr expr) {
    this(expr, null);
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {
    ((JLValue) jexpr()).store(mv, jexpr(rhs));
  }

  @Override
  public SignedByteExpr plus(GExpr operand) {
    return lift(i2b(Expressions.sum(jexpr(), jexpr(operand))));
  }

  @Override
  public SignedByteExpr minus(GExpr operand) {
    return lift(i2b(Expressions.difference(jexpr(), jexpr(operand))));
  }

  @Override
  public SignedByteExpr multiply(GExpr operand) {
    return lift(i2b(Expressions.product(jexpr(), jexpr(operand))));
  }

  @Override
  public SignedByteExpr divide(GExpr operand) {
    return lift(i2b(Expressions.divide(jexpr(), jexpr(operand))));
  }

  @Override
  public SignedByteExpr negative() {
    return lift(i2b(Expressions.negative(jexpr())));
  }

  @Override
  public SignedByteExpr min(GExpr operand) {
    return lift(Expressions.staticMethodCall(Math.class, "min", "(II)I"));
  }

  @Override
  public SignedByteExpr max(GExpr operand) {
    return lift(Expressions.staticMethodCall(Math.class, "max", "(II)I"));
  }

  @Override
  public SignedByteExpr absoluteValue() {
    return lift(Expressions.staticMethodCall(Math.class, "abs", "(I)I"));
  }

  @Override
  public SignedByteExpr remainder(GExpr operand) {
    return lift(i2b(Expressions.remainder(jexpr(), jexpr(operand))));
  }

  @Override
  public ConditionGenerator compareTo(GimpleOp op, GExpr operand) {
    return new IntegerComparison(op, jexpr(), jexpr(operand));
  }

  @Override
  public SignedByteExpr bitwiseXor(GExpr operand) {
    return lift(i2b(Expressions.bitwiseXor(jexpr(), jexpr(operand))));
  }

  @Override
  public SignedByteExpr bitwiseNot() {
    return lift(i2b(Expressions.bitwiseXor(jexpr(), 0xFF)));
  }

  @Override
  public SignedByteExpr bitwiseAnd(GExpr operand) {
    return lift(i2b(Expressions.bitwiseAnd(jexpr(), jexpr(operand))));
  }

  @Override
  public SignedByteExpr bitwiseOr(GExpr operand) {
    return lift(i2b(Expressions.bitwiseOr(jexpr(), jexpr(operand))));
  }

  @Override
  public SignedByteExpr shiftLeft(GExpr operand) {
    return lift(i2b(Expressions.shiftLeft(jexpr(), jexpr(operand))));
  }

  @Override
  public SignedByteExpr shiftRight(GExpr operand) {
    return lift(i2b(Expressions.shiftRight(jexpr(), jexpr(operand))));
  }

  @Override
  public SignedByteExpr rotateLeft(GExpr operand) {
    throw new UnsupportedOperationException("TODO");
  }


  @Override
  public GimplePrimitiveType getType() {
    return new GimpleIntegerType(8);
  }

  @Override
  public RealExpr toRealExpr() {
    return toReal(32);
  }

  @Override
  public IntExpr toSignedInt(int precision) {
    switch (precision) {
      case 8:
        return this;
      case 16:
        return new ShortExpr(jexpr());
      case 32:
        return new SignedIntExpr(jexpr());
      case 64:
        return new SignedLongExpr(i2l(jexpr()));
    }
    throw new IllegalArgumentException("signed" + precision);
  }

  @Override
  public IntExpr toUnsignedInt(int precision) {
    switch (precision) {
      case 8:
        return new UnsignedSmallIntExpr(8, Expressions.bitwiseAnd(jexpr(), 0xFF));
      case 16:
        return new UnsignedSmallIntExpr(16, Expressions.i2c(jexpr()));
      case 32:
        return new UnsignedIntExpr(jexpr());
      case 64:
        return new UnsignedLongExpr(Expressions.i2l(jexpr()));
    }
    throw new UnsupportedOperationException("unsigned" + precision);
  }

  @Override
  public RealExpr toReal(int precision) {
    return new RealExpr(new GimpleRealType(32), Expressions.i2f(jexpr())).toReal(precision);
  }

  private JExpr jexpr(GExpr operand) {
    return operand.toPrimitiveExpr().toSignedInt(8).jexpr();
  }

  private SignedByteExpr lift(JExpr expr) {
    return new SignedByteExpr(expr);
  }

  @Override
  public BooleanExpr toBooleanExpr() {
    return BooleanExpr.fromInt(jexpr());
  }
}

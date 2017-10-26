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
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.type.GimpleRealType;

import javax.annotation.Nullable;

import static org.renjin.gcc.codegen.expr.Expressions.i2b;
import static org.renjin.gcc.codegen.expr.Expressions.i2s;

/**
 * Signed 16-bit value
 */
public class ShortExpr extends AbstractIntExpr implements IntExpr {

  public ShortExpr(JExpr expr, @Nullable GExpr address) {
    super(expr, address);
  }

  public ShortExpr(JExpr expr) {
    this(expr, null);
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {
    ((JLValue) jexpr()).store(mv, jexpr(rhs));
  }

  @Override
  public ShortExpr plus(GExpr operand) {
    return lift(i2s(Expressions.sum(jexpr(), jexpr(operand))));
  }

  @Override
  public ShortExpr minus(GExpr operand) {
    return lift(i2s(Expressions.difference(jexpr(), jexpr(operand))));
  }

  @Override
  public ShortExpr multiply(GExpr operand) {
    return lift(i2s(Expressions.product(jexpr(), jexpr(operand))));
  }

  @Override
  public ShortExpr divide(GExpr operand) {
    return lift(i2s(Expressions.divide(jexpr(), jexpr(operand))));
  }

  @Override
  public ShortExpr negative() {
    return lift(i2s(Expressions.negative(jexpr())));

  }

  @Override
  public ShortExpr min(GExpr operand) {
    return lift(Expressions.staticMethodCall(Math.class, "min", "(II)I"));
  }

  @Override
  public ShortExpr max(GExpr operand) {
    return lift(Expressions.staticMethodCall(Math.class, "max", "(II)I"));
  }

  @Override
  public ShortExpr absoluteValue() {
    return lift(Expressions.staticMethodCall(Math.class, "abs", "(I)I"));
  }

  @Override
  public ShortExpr remainder(GExpr operand) {
    return lift(i2s(Expressions.remainder(jexpr(), jexpr(operand))));
  }

  @Override
  public ConditionGenerator compareTo(GimpleOp op, GExpr operand) {
    return new IntegerComparison(op, jexpr(), jexpr(operand));
  }

  @Override
  public ShortExpr bitwiseNot() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public ShortExpr bitwiseAnd(GExpr operand) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public ShortExpr bitwiseOr(GExpr operand) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public ShortExpr bitwiseXor(GExpr operand) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public ShortExpr shiftLeft(GExpr operand) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public ShortExpr shiftRight(GExpr operand) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public ShortExpr rotateLeft(GExpr operand) {
    throw new UnsupportedOperationException("TODO");
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
        return new SignedByteExpr(i2b(jexpr()));
      case 16:
        return this;
      case 32:
        return new SignedIntExpr(jexpr());
    }
    throw new IllegalArgumentException("precision: " + precision);
  }

  @Override
  public IntExpr toUnsignedInt(int precision) {
    switch (precision) {
      case 8:
        return new UnsignedIntExpr(Expressions.bitwiseAnd(jexpr(), 0xFF));
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
    return operand.toPrimitiveExpr().toSignedInt(16).jexpr();
  }

  private ShortExpr lift(JExpr expr) {
    return new ShortExpr(expr);
  }

}

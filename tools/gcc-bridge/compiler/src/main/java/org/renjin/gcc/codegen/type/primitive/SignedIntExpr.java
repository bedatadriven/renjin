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

import static org.renjin.gcc.codegen.expr.Expressions.constantInt;
import static org.renjin.gcc.codegen.expr.Expressions.staticMethodCall;

/**
 * Signed 32-bit integers.
 */
public class SignedIntExpr extends AbstractIntExpr implements IntExpr {


  public SignedIntExpr(JExpr expr, @Nullable GExpr address) {
    super(expr, address);
  }

  public SignedIntExpr(JExpr expr) {
    this(expr, null);
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {
    ((JLValue) jexpr()).store(mv, jexpr(rhs));
  }


  @Override
  public IntExpr plus(GExpr operand) {
    return lift(Expressions.sum(jexpr(), jexpr(operand)));
  }

  @Override
  public IntExpr minus(GExpr operand) {
    return lift(Expressions.difference(jexpr(), jexpr(operand)));
  }

  @Override
  public IntExpr multiply(GExpr operand) {
    return lift(Expressions.product(jexpr(), jexpr(operand)));
  }

  @Override
  public IntExpr divide(GExpr operand) {
    return lift(Expressions.divide(jexpr(), jexpr(operand)));
  }

  @Override
  public IntExpr negative() {
    return lift(Expressions.negative(jexpr()));
  }

  @Override
  public IntExpr min(GExpr operand) {
    return lift(staticMethodCall(Math.class, "min", "(II)I", jexpr(), jexpr(operand)));
  }

  @Override
  public IntExpr max(GExpr operand) {
    return lift(staticMethodCall(Math.class, "max", "(II)I", jexpr(), jexpr(operand)));
  }

  @Override
  public IntExpr absoluteValue() {
    return lift(staticMethodCall(Math.class, "abs", "(I)I", jexpr()));
  }

  @Override
  public IntExpr remainder(GExpr operand) {
    return lift(Expressions.remainder(jexpr(), jexpr(operand)));
  }

  @Override
  public ConditionGenerator compareTo(GimpleOp op, GExpr operand) {
    return new IntegerComparison(op, jexpr(), jexpr(operand));
  }

  @Override
  public IntExpr bitwiseExclusiveOr(GExpr operand) {
    return lift(Expressions.bitwiseXor(jexpr(), jexpr(operand)));
  }

  @Override
  public IntExpr bitwiseNot() {
    return lift(Expressions.bitwiseXor(jexpr(), constantInt(-1)));
  }

  @Override
  public IntExpr bitwiseAnd(GExpr operand) {
    return lift(Expressions.bitwiseAnd(jexpr(), jexpr(operand)));
  }

  @Override
  public IntExpr bitwiseOr(GExpr operand) {
    return lift(Expressions.bitwiseOr(jexpr(), jexpr(operand)));
  }

  @Override
  public IntExpr shiftLeft(GExpr operand) {
    return lift(Expressions.shiftLeft(jexpr(), jexpr(operand)));
  }

  @Override
  public IntExpr shiftRight(GExpr operand) {
    return lift(Expressions.shiftRight(jexpr(), jexpr(operand)));
  }

  @Override
  public IntExpr rotateLeft(GExpr operand) {
    return lift(Expressions.staticMethodCall(Integer.class, "rotateLeft", "(II)I", jexpr(), jexpr(operand)));
  }


  @Override
  public RealExpr toRealExpr() {
    return toReal(32);
  }

  @Override
  public IntExpr toSignedInt(int precision) {
    switch (precision) {
      case 8:
        return new SignedByteExpr(Expressions.i2b(jexpr()));
      case 16:
        return new ShortExpr(Expressions.i2s(jexpr()));
      case 32:
        return this;
      case 64:
        return new SignedLongExpr(Expressions.i2l(jexpr()));
    }
    throw new IllegalArgumentException("precision" + precision);
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
        return new SignedLongExpr(Expressions.i2l(jexpr()));
    }
    throw new UnsupportedOperationException("unsigned" + precision);
  }

  @Override
  public RealExpr toReal(int precision) {
    return new RealExpr(new GimpleRealType(32), Expressions.i2f(jexpr())).toReal(precision);
  }

  private JExpr jexpr(GExpr operand) {
    return operand.toPrimitiveExpr().toSignedInt(32).jexpr();
  }

  private IntExpr lift(JExpr expr) {
    return new SignedIntExpr(expr);
  }

}

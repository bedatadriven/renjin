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
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleRealType;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nullable;

/**
 * Signed 64-bit integers.
 */
public class SignedLongExpr extends AbstractIntExpr {

  public SignedLongExpr(JExpr expr, @Nullable PtrExpr address) {
    super(expr, address);
    assert expr.getType().equals(Type.LONG_TYPE);
  }

  public SignedLongExpr(JExpr expr) {
    this(expr, null);
  }

  @Override
  public SignedLongExpr plus(GExpr operand) {
    return lift(Expressions.sum(jexpr(), jexpr(operand)));
  }

  @Override
  public SignedLongExpr minus(GExpr operand) {
    return lift(Expressions.difference(jexpr(), jexpr(operand)));
  }

  @Override
  public SignedLongExpr multiply(GExpr operand) {
    return lift(Expressions.product(jexpr(), jexpr(operand)));
  }

  @Override
  public SignedLongExpr divide(GExpr operand) {
    return lift(Expressions.divide(jexpr(), jexpr(operand)));
  }

  @Override
  public SignedLongExpr negative() {
    return lift(Expressions.negative(jexpr()));
  }

  @Override
  public SignedLongExpr min(GExpr operand) {
    return lift(Expressions.staticMethodCall(Math.class, "min", "(JJ)J", jexpr(), jexpr(operand)));
  }

  @Override
  public SignedLongExpr max(GExpr operand) {
    return lift(Expressions.staticMethodCall(Math.class, "max", "(JJ)J", jexpr(), jexpr(operand)));
  }

  @Override
  public SignedLongExpr absoluteValue() {
    return lift(Expressions.staticMethodCall(Math.class, "abs", "(J)J", jexpr()));
  }

  @Override
  public SignedLongExpr remainder(GExpr operand) {
    return lift(Expressions.remainder(jexpr(), jexpr(operand)));
  }

  @Override
  public SignedLongExpr bitwiseXor(GExpr operand) {
    return lift(Expressions.bitwiseXor(jexpr(), jexpr(operand)));
  }

  @Override
  public SignedLongExpr bitwiseNot() {
    return lift(Expressions.bitwiseXor(jexpr(), Expressions.constantLong(-1L)));
  }

  @Override
  public SignedLongExpr bitwiseAnd(GExpr operand) {
    return lift(Expressions.bitwiseAnd(jexpr(), jexpr(operand)));
  }

  @Override
  public SignedLongExpr bitwiseOr(GExpr operand) {
    return lift(Expressions.bitwiseOr(jexpr(), jexpr(operand)));
  }

  @Override
  public SignedLongExpr shiftLeft(GExpr operand) {
    return lift(Expressions.shiftLeft(jexpr(), bits(operand)));
  }

  @Override
  public SignedLongExpr shiftRight(GExpr operand) {
    return lift(Expressions.shiftRight(jexpr(), bits(operand)));
  }

  @Override
  public SignedLongExpr rotateLeft(GExpr operand) {
    return lift(Expressions.staticMethodCall(Long.class, "rotateLeft", "(JI)J", jexpr(), bits(operand)));
  }

  public IntExpr toSignedInt32() {
    return new SignedIntExpr(Expressions.l2i(jexpr()));
  }


  @Override
  public GimplePrimitiveType getType() {
    return new GimpleIntegerType(64);
  }

  @Override
  public RealExpr toRealExpr() {
    return toReal(64);
  }

  @Override
  public IntExpr toSignedInt(int precision) {
    if(precision == 64) {
      return this;
    } else {
      return toSignedInt32().toSignedInt(precision);
    }
  }

  @Override
  public IntExpr toUnsignedInt(int precision) {
    if(precision == 64) {
      return this;
    } else if(precision <= 32) {
      return new UnsignedIntExpr(Expressions.l2i(jexpr())).toUnsignedInt(precision);
    } else {
      throw new UnsupportedOperationException("precision: " + precision);
    }
  }

  @Override
  public RealExpr toReal(int precision) {
    switch (precision) {
      case 32:
        return new RealExpr(new GimpleRealType(32), Expressions.l2f(jexpr()));
      case 64:
      case 96:
        return new RealExpr(new GimpleRealType(precision), Expressions.l2d(jexpr()));
      default:
        throw new IllegalArgumentException("precision: " + precision);
    }
  }

  @Override
  public ConditionGenerator compareTo(GimpleOp op, GExpr operand) {
    return new Comparison(op, Expressions.lcmp(jexpr(), jexpr(operand)));
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {
    ((JLValue) jexpr()).store(mv, jexpr(rhs));
  }

  private JExpr jexpr(GExpr rhs) {
    return rhs.toPrimitiveExpr().toSignedInt(64).jexpr();
  }


  private SignedLongExpr lift(JExpr expr) {
    return new SignedLongExpr(expr);
  }

  @Override
  public BooleanExpr toBooleanExpr() {
    return toSignedInt32().toBooleanExpr();
  }
}

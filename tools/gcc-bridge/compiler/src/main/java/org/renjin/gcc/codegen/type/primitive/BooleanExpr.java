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
import org.renjin.gcc.codegen.type.NumericExpr;
import org.renjin.gcc.gimple.GimpleOp;

import javax.annotation.Nullable;

/**
 * Logical boolean. Stored as 32-bit signed integer on the stack, something
 * else in arrays and fields...
 */
public class BooleanExpr extends AbstractPrimitiveExpr implements IntExpr {


  public BooleanExpr(JExpr expr, @Nullable PtrExpr address) {
    super(expr, address);
  }

  public BooleanExpr(JExpr jexpr) {
    this(jexpr, null);
  }

  public static BooleanExpr fromInt(JExpr expr) {
    return new BooleanExpr(Expressions.bitwiseAnd(expr, 0x1));
  }

  @Override
  public IntExpr toIntExpr() {
    return this;
  }

  @Override
  public RealExpr toRealExpr() {
    return toNumericExpr().toRealExpr();
  }

  @Override
  public BooleanExpr toBooleanExpr() {
    return this;
  }

  @Override
  public GExpr bitwiseXor(GExpr operand) {
    return lift(Expressions.bitwiseXor(jexpr(), jexpr(operand)));
  }

  @Override
  public GExpr bitwiseNot() {
    return lift(Expressions.bitwiseXor(jexpr(), 0x1));
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
    throw new UnsupportedOperationException();
  }

  @Override
  public GExpr shiftRight(GExpr operand) {
    throw new UnsupportedOperationException();
  }

  @Override
  public GExpr rotateLeft(GExpr operand) {
    throw new UnsupportedOperationException();
  }

  public UnsignedSmallIntExpr toUnsignedByteExpr() {
    return new UnsignedSmallIntExpr(8, jexpr());
  }

  @Override
  public IntExpr toSignedInt(int precision) {
    return toUnsignedByteExpr().toSignedInt(precision);
  }

  @Override
  public IntExpr toUnsignedInt(int precision) {
    return toUnsignedByteExpr().toSignedInt(precision);
  }

  @Override
  public RealExpr toReal(int precision) {
    return toUnsignedByteExpr().toReal(precision);
  }

  @Override
  public ConditionGenerator compareTo(GimpleOp op, GExpr operand) {
    return new IntegerComparison(op, jexpr(), jexpr(operand));
  }

  private JExpr jexpr(GExpr operand) {
    return operand.toPrimitiveExpr().toBooleanExpr().jexpr();
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {
    ((JLValue) jexpr()).store(mv, jexpr(rhs));
  }

  @Override
  public NumericExpr toNumericExpr() {
    return new SignedIntExpr(jexpr());
  }


  private BooleanExpr lift(JExpr jExpr) {
    return new BooleanExpr(jExpr);
  }

}

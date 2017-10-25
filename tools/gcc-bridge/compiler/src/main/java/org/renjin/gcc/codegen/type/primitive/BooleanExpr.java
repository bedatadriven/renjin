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
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.type.NumericExpr;
import org.renjin.gcc.gimple.GimpleOp;

import javax.annotation.Nullable;

/**
 * Logical boolean. Stored as 32-bit signed integer on the stack, something
 * else in arrays and fields...
 */
public class BooleanExpr extends AbstractPrimitiveExpr {


  public BooleanExpr(JExpr expr, @Nullable GExpr address) {
    super(expr, address);
  }

  public BooleanExpr(JExpr jexpr) {
    this(jexpr, null);
  }

  @Override
  public IntExpr toIntExpr() {
    return new SignedIntExpr(jexpr());
  }

  @Override
  public RealExpr toRealExpr() {
    return toIntExpr().toRealExpr();
  }

  @Override
  public BooleanExpr toBooleanExpr() {
    return this;
  }

  @Override
  public IntExpr toSignedInt(int precision) {
    return toIntExpr().toSignedInt(precision);
  }

  @Override
  public IntExpr toUnsignedInt(int precision) {
    return toIntExpr().toUnsignedInt(precision);
  }

  @Override
  public RealExpr toReal(int precision) {
    return toIntExpr().toReal(precision);
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
    return toIntExpr();
  }


  public GExpr logicalNot(GExpr operand) {
    throw new UnsupportedOperationException("TODO");
  }

  public GExpr logicalAnd(GExpr operand) {
    throw new UnsupportedOperationException("TODO");
  }

  public GExpr logicalOr(GExpr operand) {
    throw new UnsupportedOperationException("TODO");
  }

  public GExpr logicalExclusiveOr(GExpr operand) {
    throw new UnsupportedOperationException("TODO");
  }

}

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
package org.renjin.compiler.ir.tac.expressions;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.ConstantExpr;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.sexp.*;

import java.util.Map;


/**
 * A value known at compile time.
 */
public final class Constant implements SimpleExpression {

  public static final Constant NULL = new Constant(Null.INSTANCE);
  public static final Constant TRUE = new Constant(Logical.TRUE);
  public static final Constant FALSE = new Constant(Logical.FALSE);
  public static final Constant NA = new Constant(Logical.NA);

  private SEXP value;
  private ValueBounds valueBounds;

  public Constant(SEXP value) {
    this.value = value;
    this.valueBounds = ValueBounds.of(value);
  }
  
  public Constant(int value) {
    this(IntVector.valueOf(value));
  }

  public Constant(Logical value) {
    this(LogicalVector.valueOf(value));
  }

  public SEXP getValue() {
    return value;
  }

  @Override
  public final int getChildCount() {
    return 0;
  }

  @Override
  public final Expression childAt(int index) {
    throw new IllegalArgumentException();
  }

  @Override
  public final boolean isPure() {
    return true;
  }

  @Override
  public final void setChild(int i, Expression expr) {
    throw new IllegalArgumentException();
  }

  @Override
  public ValueBounds updateTypeBounds(Map<Expression, ValueBounds> typeMap) {
    return valueBounds;
  }

  @Override
  public ValueBounds getValueBounds() {
    return valueBounds;
  }

  @Override
  public CompiledSexp getCompiledExpr(EmitContext emitContext) {
    return ConstantExpr.createConstantExpr(value);
  }

  @Override
  public String toString() {
    return value.toString();
  }
}

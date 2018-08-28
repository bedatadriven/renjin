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
import org.renjin.compiler.ir.ValueBounds;

import java.util.Map;

/**
 * An {@code SimpleExpression} that can be the target of an assignment.
 */
public abstract class LValue implements SimpleExpression {

  private ValueBounds valueBounds = ValueBounds.UNBOUNDED;

  @Override
  public final int getChildCount() {
    return 0;
  }

  @Override
  public final Expression childAt(int index) {
    throw new IllegalArgumentException();
  }

  @Override
  public final void setChild(int i, Expression expr) {
    throw new IllegalArgumentException();
  }

  public void update(ValueBounds valueBounds) {
    this.valueBounds = valueBounds;
  }

  @Override
  public final ValueBounds updateTypeBounds(Map<Expression, ValueBounds> typeMap) {
    ValueBounds type = typeMap.get(this);
    if(type == null) {
      valueBounds = ValueBounds.UNBOUNDED;
    } else {
      valueBounds = type;
    }
    return valueBounds;
  }

  @Override
  public final ValueBounds getValueBounds() {
    return valueBounds;
  }

  @Override
  public CompiledSexp getCompiledExpr(EmitContext emitContext) {
    throw new UnsupportedOperationException("TODO");
  }

}


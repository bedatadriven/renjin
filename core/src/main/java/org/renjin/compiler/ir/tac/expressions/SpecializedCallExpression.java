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

import org.renjin.compiler.ir.ValueBounds;

import java.util.Map;

public abstract class SpecializedCallExpression implements Expression {
  protected final Expression[] arguments;

  public SpecializedCallExpression(Expression... arguments) {
    this.arguments = arguments;
  }

  @Override
  public final void setChild(int childIndex, Expression child) {
    arguments[childIndex] = child;
  }

  @Override
  public final int getChildCount() {
    return arguments.length;
  }

  @Override
  public final Expression childAt(int index) {
    return arguments[index];
  }
  
  public abstract boolean isFunctionDefinitelyPure();

  @Override
  public boolean isPure() {
    if(!isFunctionDefinitelyPure()) {
      return false;
    }
    for(int i=0;i!=arguments.length;++i) {
      if(!arguments[i].isPure()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public ValueBounds updateTypeBounds(Map<Expression, ValueBounds> typeMap) {
    return ValueBounds.UNBOUNDED;
  }

}

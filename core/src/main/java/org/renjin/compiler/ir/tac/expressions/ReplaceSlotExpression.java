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
package org.renjin.compiler.ir.tac.expressions;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.Symbol;

import java.util.Map;

public class ReplaceSlotExpression implements Expression {
  
  private Expression object;
  private Expression value;
  private final Symbol name;
  
  private ValueBounds valueBounds = ValueBounds.UNBOUNDED;
  
  public ReplaceSlotExpression(Expression object, Expression value, Symbol name) {
    this.object = object;
    this.value = value;
    this.name = name;
  }
  
  @Override
  public boolean isPure() {
    return true;
  }
  
  @Override
  public int load(EmitContext emitContext, InstructionAdapter mv) {
    throw new UnsupportedOperationException("TODO");
  }
  
  @Override
  public Type getType() {
    throw new UnsupportedOperationException("TODO");
  }
  
  @Override
  public ValueBounds updateTypeBounds(Map<Expression, ValueBounds> typeMap) {
    throw new UnsupportedOperationException("TODO");
  }
  
  @Override
  public ValueBounds getValueBounds() {
    return valueBounds;
  }
  
  @Override
  public void setChild(int childIndex, Expression child) {
    if(childIndex == 0) {
      this.object = child;
    } else if(childIndex == 1) {
      this.value = child;
    } else {
      throw new IllegalArgumentException("childIndex: " + childIndex);
    }
  }
  
  @Override
  public int getChildCount() {
    return 2;
  }
  
  @Override
  public Expression childAt(int childIndex) {
    if(childIndex == 0) {
      return object;
    } else if(childIndex == 1) {
      return value;
    } else {
      throw new IllegalArgumentException("childIndex: " + childIndex);
    }
  }
  
  @Override
  public String toString() {
    return "replaceSlot(" + object + ", " + name + ", " + value + ")";
  }
}

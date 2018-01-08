/**
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
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.Symbol;

import java.util.Map;

/**
 * Slot access in the form x@name
 */
public class NamedSlotAccess implements Expression {

  private Expression expression;
  private Symbol slot;
  private ValueBounds valueBounds;

  public NamedSlotAccess(Expression expression, String slot) {
    this.expression = expression;
    this.slot = Symbol.get(slot);
    
    valueBounds = ValueBounds.UNBOUNDED;
  }

  @Override
  public boolean isPure() {
    return true;
  }

  @Override
  public int load(EmitContext emitContext, InstructionAdapter mv) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ValueBounds updateTypeBounds(Map<Expression, ValueBounds> typeMap) {
    
    ValueBounds object = typeMap.get(expression);
  
    if(object.isAttributeConstant(slot)) {
      valueBounds = ValueBounds.of(object.getAttributeIfConstant(slot));
    } else {
      throw new UnsupportedOperationException("TODO: ");
    }
    
    
    
    return valueBounds;
  }

  @Override
  public Type getType() {
    return valueBounds.storageType();
  }

  @Override
  public ValueBounds getValueBounds() {
    return valueBounds;
  }


  @Override
  public void setChild(int childIndex, Expression child) {
    if(childIndex != 0) {
      throw new IllegalArgumentException("childIndex:" + childIndex);
    }
    expression = child;
  }

  @Override
  public int getChildCount() {
    return 1;
  }

  @Override
  public Expression childAt(int index) {
    return expression;
  }


  @Override
  public String toString() {
    return expression + "@" + slot;
  }

}

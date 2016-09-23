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
import org.renjin.sexp.SEXP;

import java.util.Map;


public class ReadLoopVector implements Expression {
  
  private ValueBounds bounds;

  public ReadLoopVector(ValueBounds bounds) {
    this.bounds = bounds;
  }

  public ReadLoopVector(SEXP elements) {
    bounds = ValueBounds.of(elements);
  }

  @Override
  public boolean isDefinitelyPure() {
    return true;
  }

  @Override
  public int load(EmitContext emitContext, InstructionAdapter mv) {
    mv.load(emitContext.getLoopVectorIndex(), Type.getType(SEXP.class));
    return 1;
  }

  @Override
  public Type getType() {
    return Type.getType(SEXP.class);
  }

  @Override
  public ValueBounds updateTypeBounds(Map<Expression, ValueBounds> typeMap) {
    return bounds;
  }

  @Override
  public ValueBounds getValueBounds() {
    return bounds;
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    throw new IllegalArgumentException("no children");
  }

  @Override
  public int getChildCount() {
    return 0;
  }

  @Override
  public Expression childAt(int index) {
    throw new IllegalArgumentException("no children");
  }

  @Override
  public String toString() {
    return "readLoopVector()";
  }
}

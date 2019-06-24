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
package org.renjin.compiler.ir.tac.expressions;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;


/**
 * Extracts a single element from a vector, dropping all attributes.
 *
 * <p>This is used to compile the extraction of the element from the for loops, so it is not meant to have
 * the full semantics of R subset operator.
 */
public class ElementAccess extends SpecializedCallExpression {

  private ValueBounds valueBounds = ValueBounds.UNBOUNDED;
  
  public ElementAccess(Expression vector, Expression index) {
    super(vector, index);
  }

  public Expression getVector() {
    return arguments[0];
  }

  /**
   * @return the value holding the zero-based index of the
   * element to extract
   */
  public Expression getIndex() {
    return arguments[1];
  }

  @Override
  public String toString() {
    return getVector() + "[" + getIndex() + "]";
  }

  @Override
  public boolean isFunctionDefinitelyPure() {
    return true;
  }

  @Override
  public ValueBounds updateTypeBounds(ValueBoundsMap typeMap) {
    ValueBounds vectorBounds = getVector().updateTypeBounds(typeMap);

    if(TypeSet.isDefinitelyAtomic(vectorBounds.getTypeSet())) {
      valueBounds = ValueBounds.builder()
          .setTypeSet(vectorBounds.getTypeSet())
          .addFlags(ValueBounds.LENGTH_ONE)
          .addFlagsFrom(vectorBounds, ValueBounds.FLAG_NO_NA | ValueBounds.FLAG_POSITIVE)
          .build();
    } else {
      valueBounds = ValueBounds.UNBOUNDED;
    }
    return valueBounds;
  }

  @Override
  public ValueBounds getValueBounds() {
    return valueBounds;
  }

  @Override
  public CompiledSexp getCompiledExpr(EmitContext emitContext) {
    CompiledSexp vectorExpr = getVector().getCompiledExpr(emitContext);
    CompiledSexp indexExpr = getIndex().getCompiledExpr(emitContext);

    return vectorExpr.elementAt(emitContext, indexExpr);

  }
}
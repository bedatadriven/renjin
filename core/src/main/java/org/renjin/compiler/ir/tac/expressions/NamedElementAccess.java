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

/**
 * Element access in the form x$name
 */
public class NamedElementAccess extends SpecializedCallExpression {

  private String memberName;
  private ValueBounds valueBounds = ValueBounds.UNBOUNDED;

  public NamedElementAccess(Expression expression, String memberName) {
    super(expression);
    this.memberName = memberName;
  }

  @Override
  public boolean isDefinitelyPure() {
    return true;
  }

  @Override
  public int load(EmitContext emitContext, InstructionAdapter mv) {
    throw new UnsupportedOperationException();
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
  public boolean isFunctionDefinitelyPure() {
    return true;
  }

  @Override
  public String toString() {
    return arguments[0] + "$" + memberName;
  }
}

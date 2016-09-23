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
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;

import java.util.Map;


/**
 * The length of an expression.
 * 
 * <p>This is a bit annoying to add this to the set of expressions,
 * but we need it to translate for expressions, because the length
 * primitive is generic, but the for loop always uses the actual length of the
 * vector.
 */
public class Length extends SpecializedCallExpression implements SimpleExpression {

  public Length(Expression vector) {
    super(vector);
  }

  public Expression getVector() {
    return arguments[0];
  }

  @Override
  public boolean isFunctionDefinitelyPure() {
    return true;
  }

  @Override
  public int load(EmitContext emitContext, InstructionAdapter mv) {
    int stackSizeIncrease = getVector().load(emitContext, mv);
    mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "org/renjin/sexp/SEXP", "length", "()I", true);
    return stackSizeIncrease;
  }

  @Override
  public Type getType() {
    return Type.INT_TYPE;
  }

  @Override
  public String toString() {
    return "length(" + getVector() + ")";
  }

  @Override
  public ValueBounds updateTypeBounds(Map<Expression, ValueBounds> typeMap) {
    return ValueBounds.INT_PRIMITIVE;
  }

  @Override
  public ValueBounds getValueBounds() {
    return ValueBounds.INT_PRIMITIVE;
  }

}

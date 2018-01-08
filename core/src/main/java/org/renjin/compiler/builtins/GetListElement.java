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
package org.renjin.compiler.builtins;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

import java.util.List;

/**
 * Specialization for accessing a single element in a list,
 * for example x[[i]].
 */
public class GetListElement implements Specialization {

  private ValueBounds source;
  private ValueBounds index;

  private ValueBounds element;

  public GetListElement(ValueBounds source, ValueBounds index) {
    this.source = source;
    this.index = index;
    this.element = source.getElementBounds();
  }

  @Override
  public Type getType() {
    return Type.getType(SEXP.class);
  }

  @Override
  public ValueBounds getResultBounds() {
    return element;
  }

  @Override
  public void load(EmitContext emitContext, InstructionAdapter mv, List<IRArgument> arguments) {
    // Load the source
    Expression sourceArgument = arguments.get(0).getExpression();
    sourceArgument.load(emitContext, mv);
    emitContext.convert(mv, sourceArgument.getType(), Type.getType(ListVector.class));

    // Load the index
    Expression indexArgument = arguments.get(0).getExpression();
    indexArgument.load(emitContext, mv);
    emitContext.convert(mv, indexArgument.getType(), Type.INT_TYPE);

    mv.invokeinterface(Type.getInternalName(SEXP.class), "getElementAsSEXP",
        Type.getMethodDescriptor(Type.getType(SEXP.class), Type.INT_TYPE));

  }

  @Override
  public boolean isPure() {
    return true;
  }
}

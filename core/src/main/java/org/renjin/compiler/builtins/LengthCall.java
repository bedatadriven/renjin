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
package org.renjin.compiler.builtins;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.SEXP;

import java.util.List;


public class LengthCall implements Specialization {
  @Override
  public Type getType() {
    return Type.INT_TYPE;
  }

  @Override
  public ValueBounds getValueBounds() {
    return ValueBounds.INT_PRIMITIVE;
  }

  @Override
  public void load(EmitContext emitContext, InstructionAdapter mv, List<IRArgument> arguments) {
    Expression argument = arguments.get(0).getExpression();
    argument.load(emitContext, mv);
    emitContext.convert(mv, argument.getType(), Type.getType(SEXP.class));
    
    mv.invokeinterface(Type.getInternalName(SEXP.class), "length", 
        Type.getMethodDescriptor(Type.INT_TYPE, Type.getType(SEXP.class)));
  }
}

/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${$file.lastModified.year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */

package org.renjin.compiler.builtins;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.ArrayExpr;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.ScalarExpr;
import org.renjin.compiler.codegen.expr.VectorType;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.primitives.Summary;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;

import java.util.List;

/**
 * Specializes sum when called with a single argument known to be of type double
 */
public class SumSpecialization implements Specialization {

  private final ValueBounds resultBounds;

  public SumSpecialization(ValueBounds resultBounds) {
    this.resultBounds = resultBounds;
  }

  @Override
  public ValueBounds getResultBounds() {
    return resultBounds;
  }

  @Override
  public boolean isPure() {
    return true;
  }

  @Override
  public CompiledSexp getCompiledExpr(EmitContext emitContext, List<IRArgument> arguments) {

    CompiledSexp argument = arguments.get(0).getExpression().getCompiledExpr(emitContext);

    return new ScalarExpr(VectorType.DOUBLE) {
      @Override
      public void loadScalar(EmitContext context, InstructionAdapter mv) {
        if(argument instanceof ArrayExpr) {
          ArrayExpr arrayExpr = (ArrayExpr) argument;
          arrayExpr.loadArray(emitContext, mv, VectorType.DOUBLE);
          mv.invokestatic(Type.getInternalName(Summary.class), "sum", "([D)D", false);
        }
      }
    };
  }

}

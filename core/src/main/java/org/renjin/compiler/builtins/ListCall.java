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
package org.renjin.compiler.builtins;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.SexpExpr;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.AtomicVector;

import java.util.List;

public class ListCall implements Specialization {

  private final List<ArgumentBounds> arguments;
  private final AtomicVector names;
  private final ValueBounds resultBounds;

  public ListCall(List<ArgumentBounds> arguments, AtomicVector names, ValueBounds resultBounds) {
    this.arguments = arguments;
    this.names = names;
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
  public CompiledSexp getCompiledExpr(EmitContext emitContext) {
    return new SexpExpr() {
      @Override
      public void loadSexp(EmitContext context, InstructionAdapter mv) {
        if(arguments.size() == 2 && !arguments.get(0).isNamed() && !arguments.get(1).isNamed()) {
          arguments.get(0).getCompiledExpr(context).loadSexp(context, mv);
          arguments.get(1).getCompiledExpr(context).loadSexp(context, mv);
          mv.invokestatic("org/renjin/sexp/ListVector", "of", "(Lorg/renjin/sexp/SEXP;Lorg/renjin/sexp/SEXP;)Lorg/renjin/sexp/ListVector;", false);
        } else {
          throw new UnsupportedOperationException("TODO");
        }
      }
    };
  }
}

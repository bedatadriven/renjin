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
import org.renjin.compiler.codegen.expr.VectorType;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.primitives.subset.Subsetting;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;

import java.util.List;

public class SubsetSpecialization implements Specialization {
  private final ArgumentBounds source;
  private final List<ArgumentBounds> subscripts;
  private final ArgumentBounds drop;
  private final ValueBounds resultBounds;

  public SubsetSpecialization(ArgumentBounds source, List<ArgumentBounds> subscripts, ArgumentBounds drop) {
    this.source = source;
    this.subscripts = subscripts;
    this.drop = drop;

    ValueBounds.Builder builder = ValueBounds.builder();
    builder.setTypeSet(computeResultTypeSet(source));

    // Only dim, dimnames, names attributes *might* be preserved
    builder.addFlagsFrom(source.getBounds(), ValueBounds.MAYBE_DIM | ValueBounds.MAYBE_DIMNAMES| ValueBounds.MAYBE_NAMES);

    resultBounds = builder.build();
  }

  private int computeResultTypeSet(ArgumentBounds source) {
    int set = source.getTypeSet();
    if(TypeSet.mightBe(set, TypeSet.PAIRLIST)) {
      set |= TypeSet.LIST;
      set &= ~TypeSet.PAIRLIST;
    }
    return set;
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
    return new SexpExpr() {
      @Override
      public void loadSexp(EmitContext context, InstructionAdapter mv) {
        source.getCompiledExpr(context).loadSexp(context, mv);
        for (int i = 0; i < subscripts.size(); i++) {
          subscripts.get(i).getCompiledExpr(context).loadSexp(context, mv);
        }
        if(drop == null) {
          mv.visitInsn(Opcodes.ICONST_0);
        } else {
          drop.getCompiledExpr(context).loadScalar(context, mv, VectorType.LOGICAL);
        }
        mv.invokestatic(Type.getInternalName(Subsetting.class), "getSubset", signature(), false);
      }
    };
  }

  private String signature() {
    StringBuilder s = new StringBuilder();
    s.append("(Lorg/renjin/sexp/SEXP;");
    for (int i = 0; i < subscripts.size(); i++) {
      s.append("Lorg/renjin/sexp/SEXP;");
    }
    s.append(")Lorg/renjin/sexp/SEXP;");
    return s.toString();
  }
}

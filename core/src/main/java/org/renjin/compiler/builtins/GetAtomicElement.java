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
import org.renjin.compiler.codegen.expr.ScalarExpr;
import org.renjin.compiler.codegen.expr.VectorType;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.commons.InstructionAdapter;

import java.util.List;

/**
 * Selects a single element from within an atomic vector
 */
public class GetAtomicElement implements Specialization {

  private final ArgumentBounds source;
  private final List<ArgumentBounds> subscripts;
  private final ValueBounds resultBounds;
  private final VectorType vectorType;

  public static GetAtomicElement trySpecialize(ArgumentBounds source, List<ArgumentBounds> subscripts) {

    if(subscripts.size() != 1) {
      return null;
    }

    ArgumentBounds subscript = subscripts.get(0);

    if (!TypeSet.isSpecificAtomic(source.getTypeSet()) || source.getTypeSet() == TypeSet.NULL) {
      return null;
    }

    if (!subscript.getBounds().isFlagSet(ValueBounds.LENGTH_ONE)) {
      return null;
    }

    // The subscript *must* not be NA, and *must* be known to be positive, otherwise
    // we could end up with x[0] or x[-1] or x[NA], which does *not* produce a scalar
    if (!subscript.getBounds().isFlagSet(ValueBounds.FLAG_NO_NA & ValueBounds.FLAG_POSITIVE)) {
      return null;
    }

    // If the source *could* have a "names" attribute, then we can't
    // represent it as a scalar
    if(source.getBounds().isFlagSet(ValueBounds.MAYBE_NAMES)) {
      return null;
    }

    ValueBounds resultBounds = new ValueBounds.Builder()
        .setTypeSet(TypeSet.elementOf(source.getTypeSet()))
        .addFlags(ValueBounds.LENGTH_ONE)
        .addFlagsFrom(source.getBounds(), ValueBounds.FLAG_NO_NA | ValueBounds.FLAG_POSITIVE)
        .build();

    return new GetAtomicElement(source, subscripts, resultBounds);
  }

  private GetAtomicElement(ArgumentBounds source, List<ArgumentBounds> subscripts, ValueBounds resultBounds) {
    this.source = source;
    this.subscripts = subscripts;
    this.resultBounds = resultBounds;
    this.vectorType = VectorType.of(resultBounds.getTypeSet());
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

    CompiledSexp sourceEmitter = source.getCompiledExpr(emitContext);
    if(subscripts.size() == 1) {
      return sourceEmitter.elementAt(emitContext, zeroBasedIndex(subscripts.get(0)));
    } else {
      throw new UnsupportedOperationException("TODO");
    }
  }

  private ScalarExpr zeroBasedIndex(ArgumentBounds subscript) {
    return new ScalarExpr(VectorType.INT) {
      @Override
      public void loadScalar(EmitContext context, InstructionAdapter mv) {
        subscript.getCompiledExpr(context).loadScalar(context, mv, VectorType.INT);
        mv.visitInsn(Opcodes.ICONST_1);
        mv.visitInsn(Opcodes.ISUB);
      }
    };
  }

}
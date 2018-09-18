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

import org.renjin.compiler.codegen.BytecodeTypes;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.ArrayExpr;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.VectorType;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.primitives.subset.Subsetting;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.Symbol;

import java.util.List;

/**
 * Specialization of a subset operation known to be a matrix subset.
 */
public class SingleRowOrColumn implements Specialization {

  private static final int ROW = 0;
  private static final int COLUMN = 0;


  private final ArgumentBounds source;
  private final List<ArgumentBounds> subscripts;
  private final ValueBounds result;
  private final int dimension;

  public SingleRowOrColumn(int dimension, ArgumentBounds source, List<ArgumentBounds> subscripts, ValueBounds resultBounds) {
    this.source = source;
    this.subscripts = subscripts;
    this.result = resultBounds;
    this.dimension = dimension;
  }

  public static SingleRowOrColumn trySpecialize(ArgumentBounds source, List<ArgumentBounds> subscripts, ArgumentBounds drop) {

    // Check for matrix indexing...
    if(subscripts.size() != 2) {
      return null;
    }

    // Need to know the type of the source
    if(!TypeSet.isSpecificAtomic(source.getTypeSet())) {
      return null;
    }

    // Should be in the form m[i,] or m[,j]
    ArgumentBounds index;
    int dimension;
    if(subscripts.get(0).getBounds().isConstant(Symbol.MISSING_ARG)) {
      index = subscripts.get(1);
      dimension = COLUMN;

    } else if(subscripts.get(1).getBounds().isConstant(Symbol.MISSING_ARG)) {
      index = subscripts.get(0);
      dimension = ROW;

    } else {
      return null;
    }

    // The drop argument must be absent or known to be true,
    // otherwise we may need to handle attributes
    if(drop != null && !drop.getBounds().isConstantFlagEqualTo(true)) {
      return null;
    }

    // The index *muast* be a scalar, *not* NA, *and* known to be positive.
    if(!index.getBounds().isFlagSet(ValueBounds.LENGTH_ONE | ValueBounds.FLAG_NO_NA | ValueBounds.FLAG_POSITIVE)) {
      return null;
    }

    return new SingleRowOrColumn(dimension, source, subscripts, ValueBounds.builder()
      .setTypeSet(source.getTypeSet())
      .addFlagsFrom(source.getBounds(), ValueBounds.FLAG_NO_NA | ValueBounds.FLAG_POSITIVE)
      .build());
  }

  public ValueBounds getResultBounds() {
    return result;
  }

  @Override
  public boolean isPure() {
    return true;
  }

  @Override
  public CompiledSexp getCompiledExpr(EmitContext emitContext) {

    // Method name
    String methodName;
    if(dimension == ROW) {
      methodName = "getMatrixRow";
    } else {
      methodName = "getMatrixColumn";
    }

    // Get compiled expressions
    CompiledSexp matrix = source.getExpression().getCompiledExpr(emitContext);
    CompiledSexp index = subscripts.get(dimension).getCompiledExpr(emitContext);

    VectorType vectorType = VectorType.of(result.getTypeSet());

    return new ArrayExpr(vectorType) {
      @Override
      public void loadArray(EmitContext context, InstructionAdapter mv, VectorType vectorType) {

        matrix.loadSexp(emitContext, mv);
        index.loadScalar(emitContext, mv, VectorType.INT);


        mv.invokestatic(Type.getInternalName(Subsetting.class), methodName,
            Type.getMethodDescriptor(vectorType.getJvmArrayType(), BytecodeTypes.SEXP_TYPE, Type.INT_TYPE), false);
      }
    };
  }
}

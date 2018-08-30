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
import org.renjin.compiler.codegen.expr.ArrayExpr;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.VectorType;
import org.renjin.compiler.codegen.var.VariableStrategy;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.statements.Assignment;
import org.renjin.primitives.subset.Subsetting;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;

import java.util.List;

/**
 * Updates a single element in an atomic vector of known type with a new scalar value.
 */
public class UpdateElementCall implements Specialization {

  private ArgumentBounds inputVector;
  private ArgumentBounds subscript;
  private ArgumentBounds replacement;
  private final VectorType resultVectorType;

  public UpdateElementCall(ArgumentBounds inputVector, ArgumentBounds subscript, ArgumentBounds replacement) {
    this.inputVector = inputVector;
    this.subscript = subscript;
    this.replacement = replacement;
    this.resultVectorType = VectorType.of(inputVector.getBounds().getTypeSet());
  }

  public ValueBounds getResultBounds() {
    return inputVector.getBounds().withVaryingValues();
  }

  @Override
  public boolean isPure() {
    // Despite the name, values in R have copy-on-write semantics
    // so "update" operations have no side-effects, they return a new value.
    return true;
  }

  @Override
  public void emitAssignment(EmitContext emitContext, InstructionAdapter mv, Assignment statement, List<IRArgument> arguments) {
    Expression source = arguments.get(0).getExpression();
    boolean mutableSource = emitContext.isSafelyMutable(statement, source);

    VariableStrategy lhs = emitContext.getVariable(statement.getLHS());
    CompiledSexp rhs = getCompiledExpr(emitContext, arguments, mutableSource);

    lhs.store(emitContext, mv, rhs);
  }

  @Override
  public CompiledSexp getCompiledExpr(EmitContext emitContext, List<IRArgument> arguments) {
    return getCompiledExpr(emitContext, arguments, false);
  }

  private CompiledSexp getCompiledExpr(EmitContext emitContext, List<IRArgument> arguments, boolean mutableSource) {
    CompiledSexp compiledSource = inputVector.getCompiledExpr(emitContext);
    CompiledSexp subscript = this.subscript.getCompiledExpr(emitContext);
    CompiledSexp replacement = this.replacement.getCompiledExpr(emitContext);

    return new ArrayExpr(resultVectorType) {
      @Override
      public void loadArray(EmitContext context, InstructionAdapter mv, VectorType vectorType) {
        compiledSource.loadArray(context, mv, resultVectorType);
        subscript.loadScalar(context, mv, VectorType.INT);
        replacement.loadScalar(context, mv, resultVectorType);

        mv.invokestatic(Type.getInternalName(Subsetting.class),
            mutableSource ? "setElementMutating" : "setElement",
            Type.getMethodDescriptor(resultVectorType.getJvmArrayType(),
                resultVectorType.getJvmArrayType(),
                Type.INT_TYPE,
                resultVectorType.getJvmType()), false);
      }
    };
  }
}

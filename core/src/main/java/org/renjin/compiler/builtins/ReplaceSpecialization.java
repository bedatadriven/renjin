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
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.SexpExpr;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.eval.Context;
import org.renjin.primitives.subset.Subsetting;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;

import java.util.List;

public class ReplaceSpecialization implements Specialization {

  private final ValueBounds resultBounds;
  private final ArgumentBounds inputVector;
  private final ArgumentBounds[] subscripts;
  private final ArgumentBounds replacement;

  public ReplaceSpecialization(ArgumentBounds inputVector, ArgumentBounds[] subscripts, ArgumentBounds replacement) {
    this.inputVector = inputVector;
    this.subscripts = subscripts;
    this.replacement = replacement;

    ValueBounds.Builder builder = ValueBounds.builder()
        .setTypeSet(TypeSet.widestVectorType(inputVector.getTypeSet(), replacement.getTypeSet()))
        .addFlags(inputVector.getFlags() & replacement.getFlags() & (ValueBounds.FLAG_NO_NA | ValueBounds.FLAG_POSITIVE));

    // Attributes are mostly preserved
    // However, some subscripts can change the shape of a matrix or array
    builder.addFlagsFrom(inputVector.getBounds(), ValueBounds.MAYBE_ATTRIBUTES);

    resultBounds = builder.build();
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

        mv.visitVarInsn(Opcodes.ALOAD, context.getContextVarIndex());

        CompiledSexp inputExpr = inputVector.getCompiledExpr(context);
        inputExpr.loadSexp(context, mv);
        for (int i = 0; i < subscripts.length; i++) {
          subscripts[i].getCompiledExpr(context).loadSexp(context, mv);
        }
        replacement.getCompiledExpr(context).loadSexp(context, mv);


        mv.invokestatic(Type.getInternalName(Subsetting.class), "setSubset", signature(), false);
      }
    };
  }

  private String signature() {
    switch (subscripts.length) {
      case 1:
        return Type.getMethodDescriptor(BytecodeTypes.SEXP_TYPE, Type.getType(Context.class),
            BytecodeTypes.SEXP_TYPE,
            BytecodeTypes.SEXP_TYPE,
            BytecodeTypes.SEXP_TYPE);
      case 2:
        return Type.getMethodDescriptor(BytecodeTypes.SEXP_TYPE, Type.getType(Context.class),
            BytecodeTypes.SEXP_TYPE,
            BytecodeTypes.SEXP_TYPE,
            BytecodeTypes.SEXP_TYPE,
            BytecodeTypes.SEXP_TYPE);
      case 3:
        return Type.getMethodDescriptor(BytecodeTypes.SEXP_TYPE, Type.getType(Context.class),
            BytecodeTypes.SEXP_TYPE,
            BytecodeTypes.SEXP_TYPE,
            BytecodeTypes.SEXP_TYPE,
            BytecodeTypes.SEXP_TYPE,
            BytecodeTypes.SEXP_TYPE);
      default:
        throw new IllegalArgumentException("subscript count: " + subscripts.length);
    }
  }
}

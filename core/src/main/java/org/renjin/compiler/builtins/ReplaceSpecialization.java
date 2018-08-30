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
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;
import org.renjin.sexp.Symbols;

import java.util.List;
import java.util.Map;

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
        .setFlag(inputVector.getFlags() & replacement.getFlags() & (ValueBounds.FLAG_NO_NA | ValueBounds.FLAG_POSITIVE));

    // Attributes are mostly preserved
    // However, some subscripts can change the shape of a matrix or array

    for (Map.Entry<Symbol, SEXP> attribute : inputVector.getBounds().getAttributeBounds().entrySet()) {
      Symbol attributeName = attribute.getKey();
      if(attributeName == Symbols.DIM || attributeName == Symbols.NAMES || attributeName == Symbols.DIMNAMES) {
        builder.attributeCouldBePresent(attributeName);
      } else {
        builder.setAttribute(attributeName, attribute.getValue());
      }
    }
    builder.closeAttributes();

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

        inputVector.getCompiledExpr(context).loadSexp(context, mv);
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
      default:
        throw new IllegalArgumentException("subscript count: " + subscripts.length);
    }
  }
}

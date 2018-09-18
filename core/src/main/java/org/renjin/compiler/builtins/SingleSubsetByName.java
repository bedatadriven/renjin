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
import org.renjin.compiler.ir.Shape;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.primitives.special.DollarFunction;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;

/**
 * Specialization for {@code x$a} and {@code x[[a]]} where a is a single character string.
 *
 */
public class SingleSubsetByName implements Specialization {
  private final ArgumentBounds object;
  private final ArgumentBounds name;
  private final ValueBounds result;

  public SingleSubsetByName(ArgumentBounds object, ArgumentBounds name) {
    this.object = object;
    this.name = name;

    if(object.getTypeSet() == TypeSet.LIST && object.getBounds().getShape() != null) {
      Shape shape = object.getBounds().getShape();

    }

    if(TypeSet.isDefinitelyAtomic(object.getTypeSet())) {

      // When applied to an atomic vector, the result will be of length one
      // (unless the object is NULL), and content flags are preserved.
      // No attributes result

      this.result = ValueBounds.builder()
          .setTypeSet(object.getTypeSet())
          .addFlagsFrom(object.getBounds(), ValueBounds.FLAG_POSITIVE | ValueBounds.FLAG_NO_NA)
          .addFlags(ValueBounds.LENGTH_ONE, !TypeSet.mightBe(object.getTypeSet(), TypeSet.NULL))
          .build();

    } else {

      // For lists, environments, etc, it could be anything!

      this.result = ValueBounds.UNBOUNDED;
    }

  }

  @Override
  public ValueBounds getResultBounds() {
    return result;
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
        mv.visitVarInsn(Opcodes.ALOAD, context.getContextVarIndex());
        object.getCompiledExpr(emitContext).loadSexp(context, mv);
        name.getCompiledExpr(emitContext).loadScalar(context, mv, VectorType.STRING);
        mv.invokestatic(Type.getInternalName(DollarFunction.class), "apply",
            "(Lorg/renjin/eval/Context;Lorg/renjin/sexp/SEXP;Llang/java/String;)(Lorg/renjin/sexp/SEXP;)", false);

      }
    };
  }
}

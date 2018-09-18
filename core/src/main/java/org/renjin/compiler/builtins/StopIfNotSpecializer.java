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

import org.renjin.compiler.codegen.ConstantBytecode;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.ScalarExpr;
import org.renjin.compiler.codegen.expr.SexpExpr;
import org.renjin.compiler.codegen.expr.VectorType;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.primitives.Conditions;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.Null;

import java.util.ArrayList;
import java.util.List;

public class StopIfNotSpecializer implements BuiltinSpecializer {

  private static final ValueBounds BOUNDS = ValueBounds.builder()
      .setTypeSet(TypeSet.NULL)
      .build();

  @Override
  public String getName() {
    return "stopifnot";
  }

  @Override
  public String getGroup() {
    return null;
  }

  @Override
  public Specialization trySpecialize(RuntimeState runtimeState, List<ArgumentBounds> arguments) {

    List<ArgumentBounds> toCheck = new ArrayList<>();
    for (ArgumentBounds argument : arguments) {
      if(!argument.getBounds().isConstant() || !Conditions.allTrue(argument.getBounds().getConstantValue())) {
        toCheck.add(argument);
      }
    }

    return new Specialization() {
      @Override
      public ValueBounds getResultBounds() {
        return BOUNDS;
      }

      @Override
      public boolean isPure() {
        return toCheck.isEmpty();
      }

      @Override
      public CompiledSexp getCompiledExpr(EmitContext emitContext) {

        return new SexpExpr() {
          @Override
          public void loadSexp(EmitContext context, InstructionAdapter mv) {
            invoke(emitContext, mv, toCheck);
            ConstantBytecode.pushConstant(mv, Null.INSTANCE);
          }
        };
      }

      @Override
      public void emitExecution(EmitContext emitContext, InstructionAdapter mv) {
        invoke(emitContext, mv, toCheck);
      }
    };
  }

  private void invoke(EmitContext emitContext, InstructionAdapter mv, List<ArgumentBounds> arguments) {
    for (ArgumentBounds argument : arguments) {
      CompiledSexp compiledArg = argument.getCompiledExpr(emitContext);
      if(compiledArg instanceof ScalarExpr && ((ScalarExpr) compiledArg).getType() == VectorType.LOGICAL) {
        compiledArg.loadScalar(emitContext, mv, VectorType.LOGICAL);
        mv.visitLdcInsn("expr");
        mv.invokestatic(Type.getInternalName(Conditions.class), "stopifnot", "(ZLjava/lang/String;)V", false);
      } else {
        compiledArg.loadSexp(emitContext, mv);
        mv.visitLdcInsn("expr");
        mv.invokestatic(Type.getInternalName(Conditions.class), "stopifnot", "(Lorg/renjin/sexp/SEXP;Ljava/lang/String;)V", false);
      }
    }
  }
}

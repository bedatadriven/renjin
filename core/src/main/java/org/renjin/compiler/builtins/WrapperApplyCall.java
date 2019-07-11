/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.eval.Context;
import org.renjin.invoke.codegen.WrapperGenerator2;
import org.renjin.primitives.Primitives;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.Environment;
import org.renjin.sexp.FunctionCall;

import java.util.List;

public class WrapperApplyCall implements Specialization {

  private final Primitives.Entry primitive;
  private final List<ArgumentBounds> arguments;
  private final ValueBounds resultBounds;

  public WrapperApplyCall(Primitives.Entry primitive, List<ArgumentBounds> arguments, ValueBounds resultBounds) {
    this.primitive = primitive;
    this.arguments = arguments;
    this.resultBounds = resultBounds;
  }

  public WrapperApplyCall(Primitives.Entry primitive, List<ArgumentBounds> arguments) {
    this(primitive, arguments, ValueBounds.UNBOUNDED);
  }

  @Override
  public ValueBounds getResultBounds() {
    return resultBounds;
  }

  @Override
  public boolean isPure() {
    return false;
  }

  @Override
  public CompiledSexp getCompiledExpr(EmitContext emitContext, FunctionCall call, List<IRArgument> arguments) {
    return new SexpExpr() {
      @Override
      public void loadSexp(EmitContext context, InstructionAdapter mv) {

        // Call into R$primitive$$plus$$doApply

        mv.visitVarInsn(Opcodes.ALOAD, context.getContextVarIndex());
        mv.visitVarInsn(Opcodes.ALOAD, context.getEnvironmentVarIndex());

        for (IRArgument argument : arguments) {
          argument.getExpression().getCompiledExpr(context).loadSexp(context, mv);
        }

        mv.invokestatic(getWrapperInternalClassName(), "doApply",
            getWrapperApplySignature(arguments), false);
      }
    };
  }

  private String getWrapperInternalClassName() {
    return "org/renjin/primitives/" + WrapperGenerator2.toJavaName(primitive.name);
  }

  private String getWrapperApplySignature(List<IRArgument> arguments) {
    String sexpDescriptor = "L" + BytecodeTypes.SEXP_INTERNAL_NAME + ";";
    StringBuilder signature = new StringBuilder();
    signature.append('(');
    signature.append(Type.getDescriptor(Context.class));
    signature.append(Type.getDescriptor(Environment.class));
    for (int i = 0; i < arguments.size(); i++) {
      signature.append(sexpDescriptor);
    }
    signature.append(')');
    signature.append(sexpDescriptor);
    return signature.toString();
  }
}

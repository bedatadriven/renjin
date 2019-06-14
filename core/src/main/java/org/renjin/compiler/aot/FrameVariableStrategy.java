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
package org.renjin.compiler.aot;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.SexpExpr;
import org.renjin.compiler.codegen.var.VariableStrategy;
import org.renjin.eval.Context;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.FunctionEnvironment;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

/**
 * Stores SEXP in an fixed-length array that can be accessed by other contexts via
 * {@link FunctionEnvironment}
 */
public class FrameVariableStrategy extends VariableStrategy {

  private final Symbol name;
  private final int frameIndex;

  public FrameVariableStrategy(Symbol name, int frameIndex) {
    this.name = name;
    this.frameIndex = frameIndex;
  }

  public Symbol getName() {
    return name;
  }

  public int getFrameIndex() {
    return frameIndex;
  }

  @Override
  public CompiledSexp getCompiledExpr() {
    return new SexpExpr() {
      @Override
      public void loadSexp(EmitContext context, InstructionAdapter mv) {
        mv.visitVarInsn(Opcodes.ALOAD, context.getEnvironmentVarIndex());
        mv.visitVarInsn(Opcodes.ALOAD, context.getContextVarIndex());
        mv.iconst(frameIndex);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(FunctionEnvironment.class), "get",
            Type.getMethodDescriptor(Type.getType(SEXP.class),
                Type.getType(Context.class),
                Type.INT_TYPE), false);
      }
    };
  }

  @Override
  public void store(EmitContext emitContext, InstructionAdapter mv, CompiledSexp compiledSexp) {

    // Write to the frame variable array
    // ALOAD env
    // ILOAD frameIndex
    // <load value>
    // INVOKEVIRTUAL

    mv.visitVarInsn(Opcodes.ALOAD, emitContext.getEnvironmentVarIndex());
    mv.visitLdcInsn(frameIndex);
    compiledSexp.loadSexp(emitContext, mv);
    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(FunctionEnvironment.class), "set",
        Type.getMethodDescriptor(Type.VOID_TYPE, Type.INT_TYPE, Type.getType(SEXP.class)), false);
  }
}

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
import org.renjin.compiler.codegen.expr.ScalarExpr;
import org.renjin.compiler.codegen.expr.VectorType;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.primitives.Vectors;
import org.renjin.repackaged.asm.Handle;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;

import java.util.List;

public class DoubleBinaryArrayOp implements Specialization {

  private final JvmMethod method;
  private final ValueBounds resultBounds;
  private final ArgumentBounds x;
  private final ArgumentBounds y;

  private boolean scalarX;
  private boolean scalarY;

  public DoubleBinaryArrayOp(JvmMethod method, List<ArgumentBounds> argumentBounds, ValueBounds resultBounds) {
    this.method = method;
    this.x = argumentBounds.get(0);
    this.y = argumentBounds.get(1);
    this.scalarX = x.getBounds().isFlagSet(ValueBounds.LENGTH_ONE);
    this.scalarY = y.getBounds().isFlagSet(ValueBounds.LENGTH_ONE);
    this.resultBounds = resultBounds;
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

    if(scalarX && scalarY) {
      return new ScalarExpr(VectorType.DOUBLE) {
        @Override
        public void loadScalar(EmitContext context, InstructionAdapter mv) {
          mv.visitLdcInsn(new Handle(Opcodes.H_INVOKESTATIC, Type.getInternalName(method.getDeclaringClass()), method.getName(),
              Type.getMethodDescriptor(method.getMethod())));

          CompiledSexp cx = x.getCompiledExpr(context);
          CompiledSexp cy = y.getCompiledExpr(context);
          cx.loadScalar(context, mv, VectorType.DOUBLE);
          cy.loadScalar(context, mv, VectorType.DOUBLE);
          mv.invokestatic(Type.getInternalName(Vectors.class), "apply", "(Ljava/lang/invoke/MethodHandle;DD)D", false);
        }
      };

    } else {
      return new ArrayExpr(VectorType.DOUBLE) {
        @Override
        public void loadArray(EmitContext context, InstructionAdapter mv, VectorType vectorType) {
          mv.visitLdcInsn(new Handle(Opcodes.H_INVOKESTATIC, Type.getInternalName(method.getDeclaringClass()), method.getName(),
              Type.getMethodDescriptor(method.getMethod())));

          CompiledSexp cx = x.getCompiledExpr(context);
          CompiledSexp cy = y.getCompiledExpr(context);

          if (!scalarX && scalarY) {
            cx.loadArray(context, mv, VectorType.DOUBLE);
            cy.loadScalar(context, mv, VectorType.DOUBLE);
            mv.invokestatic(Type.getInternalName(Vectors.class), "apply", "(Ljava/lang/invoke/MethodHandle;[DD)[D", false);
          } else if (scalarX && !scalarY) {
            cx.loadScalar(context, mv, VectorType.DOUBLE);
            cy.loadArray(context, mv, VectorType.DOUBLE);
            mv.invokestatic(Type.getInternalName(Vectors.class), "apply", "(Ljava/lang/invoke/MethodHandle;D[D)[D", false);
          } else {
            cx.loadArray(context, mv, VectorType.DOUBLE);
            cy.loadArray(context, mv, VectorType.DOUBLE);
            mv.invokestatic(Type.getInternalName(Vectors.class), "apply", "(Ljava/lang/invoke/MethodHandle;[D[D)[D", false);
          }
        }
      };
    }
  }
}

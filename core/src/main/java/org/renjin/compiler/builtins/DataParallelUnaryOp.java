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

import java.lang.invoke.MethodHandle;
import java.util.List;

public class DataParallelUnaryOp implements Specialization {

  private final JvmMethod method;
  private final ValueBounds resultBounds;
  private final VectorType resultType;
  private final VectorType argumentType;
  private final ArgumentBounds x;

  private boolean scalar;
  private boolean scalarY;

  private String applyMethodName;

  public DataParallelUnaryOp(JvmMethod method, List<ArgumentBounds> argumentBounds, ValueBounds resultBounds) {
    this.method = method;
    this.x = argumentBounds.get(0);
    this.scalar = x.getBounds().isFlagSet(ValueBounds.LENGTH_ONE);
    this.resultBounds = resultBounds;
    this.resultType = VectorType.fromJvmType(method.getReturnType());
    Class<?> parameterType = method.getMethod().getParameterTypes()[0];
    this.argumentType = VectorType.fromJvmType(parameterType);

    this.applyMethodName = "apply" + code(method.getReturnType()) + code(parameterType);
    if(!method.isPassNA()) {
      this.applyMethodName += "NA";
    }
  }

  private String code(Class type) {
    return type.getSimpleName().toUpperCase().substring(0, 1);
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

    if(scalar) {
      return new ScalarExpr(resultType) {
        @Override
        public void loadScalar(EmitContext context, InstructionAdapter mv) {

          mv.visitLdcInsn(new Handle(Opcodes.H_INVOKESTATIC, Type.getInternalName(method.getDeclaringClass()), method.getName(),
              Type.getMethodDescriptor(method.getMethod())));

          CompiledSexp cx = x.getCompiledExpr(context);
          cx.loadScalar(context, mv, argumentType);
          mv.invokestatic(Type.getInternalName(Vectors.class), applyMethodName,
              Type.getMethodDescriptor(resultType.getJvmType(), Type.getType(MethodHandle.class), argumentType.getJvmType()), false);
        }
      };

    } else {
      return new ArrayExpr(resultType) {
        @Override
        public void loadArray(EmitContext context, InstructionAdapter mv, VectorType vectorType) {

          mv.visitLdcInsn(new Handle(Opcodes.H_INVOKESTATIC, Type.getInternalName(method.getDeclaringClass()), method.getName(),
              Type.getMethodDescriptor(method.getMethod())));

          CompiledSexp cx = x.getCompiledExpr(context);
          cx.loadArray(context, mv, argumentType);

          mv.invokestatic(Type.getInternalName(Vectors.class), applyMethodName,
              Type.getMethodDescriptor(resultType.getJvmArrayType(), Type.getType(MethodHandle.class), argumentType.getJvmArrayType()), false);
        }
      };
    }
  }
}

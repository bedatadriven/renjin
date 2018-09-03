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
import org.renjin.compiler.codegen.expr.ScalarExpr;
import org.renjin.compiler.codegen.expr.VectorType;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;

import java.util.Iterator;
import java.util.List;

/**
 * Call to a data parallel operator with scalar arguments. 
 */
public class DataParallelScalarCall implements Specialization {
  
  private final JvmMethod method;
  private final List<ArgumentBounds> argumentBounds;
  private final ValueBounds valueBounds;

  public DataParallelScalarCall(JvmMethod method, List<ArgumentBounds> argumentBounds, ValueBounds resultBounds) {
    this.method = method;
    this.argumentBounds = argumentBounds;
    this.valueBounds = resultBounds;
  }
  
  public Specialization trySpecializeFurther() {
    return this;
  }

  public ValueBounds getResultBounds() {
    return valueBounds;
  }

  @Override
  public boolean isPure() {
    return method.isPure();
  }

  @Override
  public CompiledSexp getCompiledExpr(EmitContext context, List<IRArgument> arguments) {
    return new ScalarExpr(vectorTypeOf(method.getReturnType())) {
      @Override
      public void loadScalar(EmitContext context, InstructionAdapter mv) {
        Iterator<ArgumentBounds> argumentIt = argumentBounds.iterator();

        for (JvmMethod.Argument formal : method.getAllArguments()) {
          if(formal.isContextual()) {
            throw new UnsupportedOperationException("TODO");

          } else if(formal.isRecycle()) {
            CompiledSexp argument = argumentIt.next().getExpression().getCompiledExpr(context);
            argument.loadAsArgument(context, mv, formal.getClazz());
          }
        }

        mv.invokestatic(Type.getInternalName(method.getDeclaringClass()), method.getName(),
            Type.getMethodDescriptor(method.getMethod()), false);
      }
    };
  }

  private VectorType vectorTypeOf(Class returnType) {
    if(returnType.equals(int.class)) {
      return VectorType.INT;
    } else if(returnType.equals(double.class)) {
      return VectorType.DOUBLE;
    } else if(returnType.equals(byte.class)) {
      return VectorType.BYTE;
    } else if(returnType.equals(boolean.class)) {
      return VectorType.LOGICAL;
    }
    throw new UnsupportedOperationException("TODO: " + returnType.getSimpleName());
  }

}

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
import org.renjin.compiler.codegen.expr.SexpExpr;
import org.renjin.compiler.codegen.expr.VectorType;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.SEXP;

import java.util.List;


/**
 * Call to a single static method overload of a builtin.
 */
public class StaticMethodCall implements Specialization {

  private final JvmMethod method;
  private final ValueBounds valueBounds;
  private final boolean pure;

  public StaticMethodCall(JvmMethod method) {
    this.method = method;
    this.pure = method.isPure();
    this.valueBounds = ValueBounds.of(method.getReturnType());
  }

  public StaticMethodCall(JvmMethod method, ValueBounds bounds) {
    this.method = method;
    this.pure = method.isPure();
    this.valueBounds = bounds;
  }

  public static boolean isEligible(JvmMethod method) {

    // Verify that this method doesn't require @Current Context or @Current Environment,
    // Such methods have side effects that the compiler can't take into account.
    for (JvmMethod.Argument argument : method.getAllArguments()) {
      if(argument.isContextual()) {
        return false;
      }
    }

    return true;
  }

  public Specialization furtherSpecialize(List<ValueBounds> argumentBounds) {
    if (pure && ValueBounds.allConstant(argumentBounds)) {
      return ConstantCall.evaluate(method, argumentBounds);
    }
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
  public CompiledSexp getCompiledExpr(EmitContext emitContext, List<IRArgument> arguments) {


    if(SEXP.class.isAssignableFrom(method.getReturnType())) {
      return new SexpExpr() {
        @Override
        public void loadSexp(EmitContext context, InstructionAdapter mv) {
          invoke(context, mv, arguments);
        }
      };
    } else if(method.getReturnType().isPrimitive() || method.getReturnType().equals(String.class)) {
      return new ScalarExpr(VectorType.fromJvmType(method.getReturnType())) {
        @Override
        public void loadScalar(EmitContext context, InstructionAdapter mv) {
          invoke(context, mv, arguments);
        }
      };
    } else {
      throw new UnsupportedOperationException("returnType: " + method.getReturnType());
    }
  }

  private void invoke(EmitContext emitContext, InstructionAdapter mv, List<IRArgument> arguments) {
    int positionalArgument = 0;
    for (JvmMethod.Argument argument : method.getAllArguments()) {
      if(argument.isContextual() || argument.isNamedFlag() || argument.isVarArg()) {
        throw new UnsupportedOperationException("TODO");
      }
      CompiledSexp compiledArg = arguments.get(positionalArgument).getExpression().getCompiledExpr(emitContext);
      compiledArg.loadAsArgument(emitContext, mv, argument.getClazz());
    }

    mv.invokestatic(
        Type.getInternalName(method.getDeclaringClass()),
        method.getName(),
        Type.getMethodDescriptor(method.getMethod()),
        false);

  }

}

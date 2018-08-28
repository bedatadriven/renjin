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
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.invoke.model.JvmMethod;

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
    throw new UnsupportedOperationException("TODO");
  }

}

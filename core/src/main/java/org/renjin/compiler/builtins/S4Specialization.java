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

import org.renjin.compiler.cfg.InlinedFunction;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.eval.MatchedArgumentPositions;
import org.renjin.repackaged.asm.Type;
import org.renjin.sexp.Closure;

import java.util.List;
import java.util.Map;

/**
 * S4 Specializer
 */
public class S4Specialization implements Specialization {
  
  
  private RuntimeState runtimeState;
  private Closure closure;
  
  
  private InlinedFunction inlinedMethod = null;
  private MatchedArgumentPositions matchedArguments;
  
  private Type type;
  private ValueBounds returnBounds;
  
  public S4Specialization(RuntimeState runtimeState, Closure closure, List<ArgumentBounds> arguments) {
    this.runtimeState = runtimeState;
    this.closure = closure;
    
    updateTypeBounds(closure, arguments);
  }
  
  public S4Specialization(RuntimeState runtimeState, Closure closure, Map<Expression, ValueBounds> typeMap, List<IRArgument> arguments) {
    this(runtimeState, closure, ArgumentBounds.create(arguments, typeMap));
  }
  
  private void updateTypeBounds(Closure function, List<ArgumentBounds> arguments) {

    // Otherwise, try to resolve the function
    if (inlinedMethod == null || inlinedMethod.getClosure() != function) {
      inlinedMethod = new InlinedFunction("g", runtimeState, closure, ArgumentBounds.names(arguments));
    }

//    if (matchedArguments.hasExtraArguments()) {
//      throw new FailedToSpecializeException("Extra arguments not supported");
//    }

    returnBounds = inlinedMethod.updateBounds(arguments);
  }
  
  public static Specialization trySpecialize(String generic, RuntimeState runtimeState, ValueBounds objectExpr, List<ArgumentBounds> arguments) {

    // TODO
    return UnspecializedCall.INSTANCE;
  }

  @Override
  public ValueBounds getResultBounds() {
    return returnBounds;
  }

  @Override
  public boolean isPure() {
    if(inlinedMethod == null) {
      return false;
    }
    return inlinedMethod.isPure();
  }

  @Override
  public CompiledSexp getCompiledExpr(EmitContext emitContext) {
    throw new UnsupportedOperationException("TODO");
  }

}
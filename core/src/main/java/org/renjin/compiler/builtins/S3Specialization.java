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

import org.renjin.compiler.cfg.InlinedFunction;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.var.VariableStrategy;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.compiler.ir.tac.statements.Assignment;
import org.renjin.primitives.S3;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.Closure;
import org.renjin.sexp.Function;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.StringVector;

import java.util.List;

public class S3Specialization implements Specialization {


  private final String generic;
  private RuntimeState runtimeState;
  private Closure closure;

  private InlinedFunction inlinedMethod = null;

  private ValueBounds returnBounds;

  public S3Specialization(String generic, RuntimeState runtimeState, Closure closure, List<ArgumentBounds> arguments) {
    this.generic = generic;
    this.runtimeState = runtimeState;
    this.closure = closure;

    updateTypeBounds(closure, arguments);
  }

  private void updateTypeBounds(Closure function, List<ArgumentBounds> arguments) {
  
    // Otherwise, try to resolve the function
    if(inlinedMethod == null || inlinedMethod.getClosure() != function) {
      inlinedMethod = new InlinedFunction(generic, runtimeState, closure, ArgumentBounds.names(arguments));
    }
  
    returnBounds = inlinedMethod.updateBounds(arguments);
  }
  
  public static Specialization trySpecialize(String generic, RuntimeState runtimeState, ValueBounds objectExpr, List<ArgumentBounds> arguments) {
    StringVector objectClass = S3.computeDataClasses(objectExpr);
  
    if (objectClass == null) {
      // We can't determine the class on which to dispatch, so we have to give up
      return UnspecializedCall.INSTANCE;
    }
  
    // Otherwise, try to resolve the function
    Function function = runtimeState.findMethod(generic, null, objectClass);
    if(function instanceof Closure) {
      return new S3Specialization(generic, runtimeState, (Closure)function, arguments);
    }
    
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
  public CompiledSexp getCompiledExpr(EmitContext emitContext, FunctionCall call, List<IRArgument> arguments) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void emitAssignment(EmitContext emitContext, InstructionAdapter mv, Assignment statement, FunctionCall call, List<IRArgument> arguments) {

    if(inlinedMethod == null) {
      throw new FailedToSpecializeException("Could not resolve S3 method");
    }

    VariableStrategy lhs = emitContext.getVariable(statement.getLHS());

    inlinedMethod.emitInline(emitContext, mv, arguments, lhs);
  }
}

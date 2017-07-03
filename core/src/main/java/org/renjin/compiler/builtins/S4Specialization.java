/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.eval.MatchedArguments;
import org.renjin.primitives.S3;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.Closure;
import org.renjin.sexp.Function;
import org.renjin.sexp.StringVector;

import java.util.List;
import java.util.Map;

/**
 * S4 Specializer
 */
public class S4Specialization implements Specialization {
  
  
  private RuntimeState runtimeState;
  private Closure closure;
  
  
  private InlinedFunction inlinedMethod = null;
  private MatchedArguments matchedArguments;
  
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
      matchedArguments = MatchedArguments.matchArgumentBounds(closure, arguments);
      inlinedMethod = new InlinedFunction(runtimeState, closure, matchedArguments.getSuppliedFormals());
    }
    
//    if (matchedArguments.hasExtraArguments()) {
//      throw new FailedToSpecializeException("Extra arguments not supported");
//    }
    
    returnBounds = inlinedMethod.updateBounds(arguments);
    type = returnBounds.storageType();
  }
  
  public static Specialization trySpecialize(String generic, RuntimeState runtimeState, ValueBounds objectExpr, List<ArgumentBounds> arguments) {
    StringVector objectClass = S3.computeDataClasses(objectExpr);
    
    if (objectClass == null) {
      // We can't determine the class on which to dispatch, so we have to give up
      return UnspecializedCall.INSTANCE;
    }
  
  
    // check that argument classes are constant
  
    // follow the same logic as with S3.handleS4object() to
    // resolve the function and assign it to inlineMethod
    
    // Otherwise, try to resolve the function
    Function function = runtimeState.findMethod(generic, null, objectClass);
    if (function instanceof Closure) {
      return new S4Specialization(runtimeState, (Closure) function, arguments);
    }
    
    return UnspecializedCall.INSTANCE;
  }
  
  @Override
  public Type getType() {
    return type;
  }
  
  @Override
  public ValueBounds getResultBounds() {
    return returnBounds;
  }
  
  @Override
  public void load(EmitContext emitContext, InstructionAdapter mv, List<IRArgument> arguments) {
    
    if (inlinedMethod == null) {
      throw new FailedToSpecializeException("Could not resolve S4 method");
    }
    
    if (matchedArguments.hasExtraArguments()) {
      throw new FailedToSpecializeException("Extra arguments not supported");
    }
    
    inlinedMethod.writeInline(emitContext, mv, matchedArguments, arguments);
    
  }
}
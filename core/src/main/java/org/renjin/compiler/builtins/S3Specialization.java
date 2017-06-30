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
import org.renjin.compiler.ir.tac.IRMatchedArguments;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.primitives.S3;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.Closure;
import org.renjin.sexp.Function;
import org.renjin.sexp.StringVector;

import java.util.List;
import java.util.Map;

/**
 * Created by parham on 8-6-17.
 */
public class S3Specialization implements Specialization {
  
  
  private RuntimeState runtimeState;
  private Closure closure;
  private List<IRArgument> arguments;
  
  
  private InlinedFunction inlinedMethod = null;
  private IRMatchedArguments matchedArguments;
  
  private Type type;
  private ValueBounds returnBounds;
  
  public S3Specialization(RuntimeState runtimeState, Closure closure, Map<Expression, ValueBounds> typeMap, List<IRArgument> arguments) {
    this.runtimeState = runtimeState;
    this.closure = closure;
    this.arguments = arguments;
  
    updateTypeBounds(closure, typeMap);
  }
  
  private void updateTypeBounds(Closure function, Map<Expression, ValueBounds> typeMap) {
  
    // Otherwise, try to resolve the function
    if(inlinedMethod == null || inlinedMethod.getClosure() != function) {
      matchedArguments = new IRMatchedArguments(closure, arguments);
      inlinedMethod = new InlinedFunction(runtimeState, closure, matchedArguments.getSuppliedFormals());
    }
    
    if(matchedArguments.hasExtraArguments()) {
      throw new FailedToSpecializeException("Extra arguments not supported");
    }
  
    returnBounds = inlinedMethod.updateBounds(arguments, typeMap);
    type = returnBounds.storageType();
  }
  
  public static Specialization trySpecialize(String generic, RuntimeState runtimeState, ValueBounds objectExpr, Map<Expression, ValueBounds> typeMap, List<IRArgument> arguments) {
    StringVector objectClass = S3.computeDataClasses(objectExpr);
  
    if (objectClass == null) {
      // We can't determine the class on which to dispatch, so we have to give up
      return UnspecializedCall.INSTANCE;
    }
  
    // Otherwise, try to resolve the function
    Function function = runtimeState.findMethod(generic, null, objectClass);
    if(function instanceof Closure) {
      return new S3Specialization(runtimeState, (Closure)function, typeMap, arguments);
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
  
    if(inlinedMethod == null) {
      throw new FailedToSpecializeException("Could not resolve S3 method");
    }
  
    if(matchedArguments.hasExtraArguments()) {
      throw new FailedToSpecializeException("Extra arguments not supported");
    }
  
    inlinedMethod.writeInline(emitContext, mv, matchedArguments, arguments);
  
  }
}

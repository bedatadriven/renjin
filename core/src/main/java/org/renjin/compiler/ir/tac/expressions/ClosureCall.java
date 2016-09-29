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
package org.renjin.compiler.ir.tac.expressions;

import org.renjin.compiler.NotCompilableException;
import org.renjin.compiler.cfg.InlinedFunction;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.compiler.ir.tac.IRMatchedArguments;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.sexp.Closure;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.Symbol;

import java.util.List;
import java.util.Map;


public class ClosureCall implements Expression {

  private RuntimeState runtimeState;
  private final FunctionCall call;
  private final List<IRArgument> arguments;
  private Closure closure;
  private IRMatchedArguments matching;
  private InlinedFunction inlinedFunction;
  
  private ValueBounds returnBounds;
  private Type type;

  public ClosureCall(RuntimeState runtimeState, FunctionCall call, Closure closure, List<IRArgument> arguments) {
    this.runtimeState = runtimeState;
    this.call = call;
    this.closure = closure;
    this.arguments = arguments;
    this.matching = new IRMatchedArguments(closure, arguments);
    this.returnBounds = ValueBounds.UNBOUNDED;
    this.type = returnBounds.storageType();
  }

  @Override
  public boolean isDefinitelyPure() {
    return false;
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public ValueBounds updateTypeBounds(Map<Expression, ValueBounds> typeMap) {

    if(inlinedFunction == null) {
      try {
        this.inlinedFunction = new InlinedFunction(runtimeState, closure, this.matching.getSuppliedFormals());
      } catch (NotCompilableException e) {
        throw new NotCompilableException(call, e);
      }
    }
    
    if(matching.hasExtraArguments()) {
      throw new NotCompilableException(call, "Extra arguments not supported");
    }
   
    returnBounds = inlinedFunction.updateBounds(arguments, typeMap);
    type = returnBounds.storageType();
    
    return returnBounds;
  }

  @Override
  public ValueBounds getValueBounds() {
    return returnBounds;
  }

  @Override
  public int load(EmitContext emitContext, InstructionAdapter mv) {


    if(matching.hasExtraArguments()) {
      throw new NotCompilableException(call, "Extra arguments not supported");
    }
    
    inlinedFunction.writeInline(emitContext, mv, matching, arguments);
    
    return 0;
  }

  
  @Override
  public void setChild(int childIndex, Expression child) {
    arguments.get(childIndex).setExpression(child);
  }

  @Override
  public int getChildCount() {
    return arguments.size();
  }

  @Override
  public Expression childAt(int index) {
    return arguments.get(index).getExpression();
  }

  @Override
  public String toString() {
    return functionName() + "(" + Joiner.on(", ").join(arguments) + ")";
  }

  private String functionName() {
    if(call.getFunction() instanceof Symbol) {
      return ((Symbol) call.getFunction()).getPrintName();
    } else {
      return "fn";
    }
  }

}

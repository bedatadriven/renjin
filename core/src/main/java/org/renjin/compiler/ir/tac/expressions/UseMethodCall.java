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
package org.renjin.compiler.ir.tac.expressions;

import org.renjin.compiler.builtins.ArgumentBounds;
import org.renjin.compiler.builtins.S3Specialization;
import org.renjin.compiler.builtins.Specialization;
import org.renjin.compiler.builtins.UnspecializedCall;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.sexp.FunctionCall;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Call to UseMethod
 */
public class UseMethodCall implements Expression {
  
  private RuntimeState runtimeState;
  private FunctionCall call;
  /**
   * The name of the generic method. 
   */
  private final String generic;
  
  private final List<IRArgument> arguments;
  
  /**
   * The object expression whose class is used to dispatch the call.
   */
  private Expression objectExpr;
  
  private Specialization specialization = UnspecializedCall.INSTANCE;
  
  public UseMethodCall(RuntimeState runtimeState, FunctionCall call, String generic, Expression objectExpr) {
    this.runtimeState = runtimeState;
    this.call = call;
    this.generic = generic;
    this.objectExpr = objectExpr;
    
    // Cheating for now because we only support unary functions...
    arguments = Collections.singletonList(new IRArgument(objectExpr));
  }

  @Override
  public boolean isPure() {
    return specialization.isPure();
  }

  @Override
  public int load(EmitContext emitContext, InstructionAdapter mv) {
    specialization.load(emitContext, mv, arguments);
    return 0;
  }

  @Override
  public Type getType() {
    return specialization.getType();
  }

  @Override
  public ValueBounds updateTypeBounds(Map<Expression, ValueBounds> typeMap) {

    ValueBounds objectBounds = typeMap.get(objectExpr);
    
    // Maybe see if we can avoid re-specializing entirely?
    this.specialization = S3Specialization.trySpecialize(generic, runtimeState, objectBounds, ArgumentBounds.create(arguments, typeMap));
    return specialization.getResultBounds();
  }

  @Override
  public ValueBounds getValueBounds() {
    return specialization.getResultBounds();
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    if(childIndex == 0) {
      objectExpr = child;
    } else {
      arguments.get(childIndex - 1).setExpression(child);
    }  
  }

  @Override
  public int getChildCount() {
    return 1 + arguments.size();
  }

  @Override
  public Expression childAt(int index) {
    if(index == 0) {
      return objectExpr;
    } else {
      return arguments.get(index - 1).getExpression();
    }
  }

  @Override
  public String toString() {
    return "UseMethod(" + generic + ", " + objectExpr + ", " + Joiner.on(", ").join(arguments) + ")";
  }
}

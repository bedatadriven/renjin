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
import org.renjin.compiler.builtins.*;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.primitives.Primitives;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.sexp.FunctionCall;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Call to a builtin function
 */
public class BuiltinCall implements CallExpression {

  private final RuntimeState runtimeState;
  private FunctionCall call;
  private String primitiveName;
  private final List<IRArgument> arguments;

  private final Specializer specializer;
  
  private Specialization specialization = UnspecializedCall.INSTANCE;

  public BuiltinCall(RuntimeState runtimeState, FunctionCall call, String primitiveName, List<IRArgument> arguments) {
    this.runtimeState = runtimeState;
    this.call = call;
    this.primitiveName = primitiveName;
    this.arguments = arguments;
    this.specializer = BuiltinSpecializers.INSTANCE.get(primitiveName);
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
  public void setChild(int childIndex, Expression child) {
    arguments.get(childIndex).setExpression(child);
  }
  
  @Override
  public boolean isDefinitelyPure() {
    return false;
  }


  @Override
  public int load(EmitContext emitContext, InstructionAdapter mv) {
    try {
      specialization.load(emitContext, mv, arguments);

    } catch (FailedToSpecializeException e) {
      throw new NotCompilableException(call, "Failed to specialize .Primitive(" + primitiveName + ")");
    }
    return 1;
  }

  @Override
  public ValueBounds updateTypeBounds(Map<Expression, ValueBounds> typeMap) {
    List<ValueBounds> argumentTypes = new ArrayList<>();
    for (IRArgument argument : arguments) {
      argumentTypes.add(argument.getExpression().updateTypeBounds(typeMap));
    }
    specialization = specializer.trySpecialize(runtimeState, argumentTypes);
    
    return specialization.getValueBounds();
  }

  @Override
  public Type getType() {
    return specialization.getType();
  }

  @Override
  public ValueBounds getValueBounds() {
    return specialization.getValueBounds();
  }
  
  @Override
  public String toString() {
    return "(" + primitiveName + " " + Joiner.on(" ").join(arguments) + ")";
  }
}

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
package org.renjin.compiler.cfg;

import org.renjin.compiler.SexpCompiler;
import org.renjin.compiler.builtins.ArgumentBounds;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.var.VariableStrategy;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.compiler.ir.tac.IRBody;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.compiler.ir.tac.expressions.ReadParam;
import org.renjin.compiler.ir.tac.statements.ReturnStatement;
import org.renjin.compiler.ir.tac.statements.Statement;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.repackaged.guava.annotations.VisibleForTesting;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.Closure;
import org.renjin.sexp.Function;

import java.util.ArrayList;
import java.util.List;


public class InlinedFunction {

  private final RuntimeState runtimeState;

  private final SexpCompiler compiler;
  private final List<ReadParam> params;

  private List<ReturnStatement> returnStatements = Lists.newArrayList();
  private final String functionName;
  private Closure closure;


  /**
   * @param functionName
   * @param closure the closure to inline
   * @param argumentNames the names of the arguments supplied to the function call
   */
  public InlinedFunction(String functionName, RuntimeState parentState, Closure closure, String[] argumentNames) {

    this.functionName = functionName;
    this.closure = closure;

    runtimeState = new RuntimeState(parentState, closure.getEnclosingEnvironment()) {
      @Override
      public int getNumArgs() {
        return argumentNames.length;
      }
    };
    
    IRBodyBuilder builder = new IRBodyBuilder(runtimeState);
    IRBody body = builder.buildFunctionBody(closure, argumentNames);

    compiler = new SexpCompiler(runtimeState, body, false);
    params = body.getParams();

    for (Statement statement : body.getStatements()) {
      if(statement instanceof ReturnStatement) {
        returnStatements.add((ReturnStatement) statement);
      }
    }
    System.out.println(compiler.getControlFlowGraph());
  }

  @VisibleForTesting
  ControlFlowGraph getCfg() {
    return compiler.getControlFlowGraph();
  }

  public List<ReadParam> getParams() {
    return params;
  }


  public void updateParam(int i, ValueBounds argumentBounds) {
    params.get(i).updateBounds(argumentBounds);
  }

  public ValueBounds updateBounds(List<ArgumentBounds> arguments) {
    for (int i = 0; i < arguments.size(); i++) {
      updateParam(i, arguments.get(i).getBounds());
    }
    return computeBounds();
  }
  
  public ValueBounds computeBounds() {
    
    compiler.updateTypes();

    List<ValueBounds> returnBounds = new ArrayList<>();
    for (ReturnStatement returnStatement : returnStatements) {
      returnBounds.add(returnStatement.getRHS().getValueBounds());
    }
    ValueBounds union = ValueBounds.union(returnBounds);

    return union;
  }

  /**
   *
   * @return true if it can be proven that this inlined function has no side effects.
   */
  public boolean isPure() {
    return compiler.isPure();
  }
  
  public void emitInline(EmitContext emitContext,
                         InstructionAdapter mv,
                         List<IRArgument> arguments,
                         VariableStrategy returnVariable) {

    List<CompiledSexp> parameters = new ArrayList<>();
    for (IRArgument argument : arguments) {
      parameters.add(argument.getExpression().getCompiledExpr(emitContext));
    }
    compiler.compileInline(emitContext, mv, parameters, returnVariable);
  }


  @Override
  public String toString() {
    return compiler.getControlFlowGraph().toString();
  }

  public Function getClosure() {
    return closure;
  }

}

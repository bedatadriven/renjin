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
package org.renjin.compiler.ir.tac.statements;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.var.VariableStrategy;
import org.renjin.compiler.ir.ssa.SsaVariable;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.EnvironmentVariable;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.LValue;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.Environment;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import java.util.Collections;
import java.util.List;


public class ReturnStatement extends Statement implements BasicBlockEndingStatement {

  private Expression returnValue;

  private List<Symbol> environmentVariableNames = Lists.newArrayList();
  private List<Expression> environmentVariables = Lists.newArrayList();

  public ReturnStatement(Expression returnValue) {
    this.returnValue = returnValue;
  }

  public Expression getReturnValue() {
    return returnValue;
  }


  @Override
  public Expression getRHS() {
    return returnValue;
  }

  @Override
  public Iterable<IRLabel> possibleTargets() {
    return Collections.emptySet();
  }

  public void addEnvironmentVariables(Iterable<EnvironmentVariable> variables) {
    for (EnvironmentVariable variable : variables) {
      addEnvironmentVariable(variable.getName(), variable);
    }
  }

  public void addEnvironmentVariable(Symbol symbol, LValue lValue) {
    environmentVariableNames.add(symbol);
    environmentVariables.add(lValue);
  }

  public List<Expression> getEnvironmentVariables() {
    return environmentVariables;
  }

  @Override
  public int getChildCount() {
    return 1 + environmentVariableNames.size();
  }

  @Override
  public Expression childAt(int index) {
    if(index == 0) {
      return returnValue;
    } else {
      return environmentVariables.get(index - 1);
    }
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    if(childIndex == 0) {
      returnValue = child;
    } else {
      environmentVariables.set(childIndex - 1, child);
    }
  }

  @Override
  public void emit(EmitContext emitContext, InstructionAdapter mv) {

    // Set the current local variables back into the environment
    for (int i = 0; i < environmentVariableNames.size(); i++) {

      Expression variable = environmentVariables.get(i);
      if(variable instanceof SsaVariable) {
        // Check if this variable has been updated at all
        SsaVariable ssaVariable = (SsaVariable) variable;
        if(ssaVariable.getVersion() > 1) {
          VariableStrategy strategy = emitContext.getVariable(ssaVariable);
          if(strategy != null) {
            // Environment.setVariable(String, SEXP)
            mv.load(emitContext.getEnvironmentVarIndex(), Type.getType(Environment.class));
            mv.aconst(environmentVariableNames.get(i).getPrintName());

            strategy.getCompiledExpr().loadSexp(emitContext, mv);

            mv.invokevirtual(Type.getInternalName(Environment.class), "setVariableUnsafe",
                Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String.class), Type.getType(SEXP.class)), false);
          }
        }
      }
    }


    CompiledSexp compiledReturnValue = returnValue.getCompiledExpr(emitContext);

    emitContext.writeReturn(mv, compiledReturnValue);
  }

  @Override
  public boolean isPure() {
    return true;
  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder();
    s.append("return ");
    s.append(returnValue);
    for (int i = 0; i < environmentVariableNames.size(); i++) {
      s.append(", ");
      s.append(environmentVariableNames.get(i));
      s.append(" = ");
      s.append(environmentVariables.get(i));
    }
    return s.toString();
  }
}

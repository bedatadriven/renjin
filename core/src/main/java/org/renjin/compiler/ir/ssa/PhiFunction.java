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
package org.renjin.compiler.ir.ssa;

import org.renjin.compiler.cfg.FlowEdge;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.Variable;
import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.repackaged.guava.collect.Lists;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PhiFunction implements Expression {

  private List<Variable> arguments;
  private List<FlowEdge> incomingEdges;

  public PhiFunction(Variable variable, List<FlowEdge> incomingEdges) {
    if(incomingEdges.size() < 2) {
      throw new IllegalArgumentException("variable=" + variable + ", count=" + incomingEdges.size() + " (count must be >= 2)");
    }
    this.incomingEdges = Lists.newArrayList(incomingEdges);
    this.arguments = Lists.newArrayList();
    for(int i=0;i!=incomingEdges.size();++i) {
      arguments.add(variable);
    }
  }

  public List<Variable> getArguments() {
    return arguments;
  }

  @Override
  public String toString() {
    return "Φ(" + Joiner.on(", ").join(arguments) + ")";
  }

  @Override
  public boolean isPure() {
    return true;
  }

  @Override
  public ValueBounds updateTypeBounds(Map<Expression, ValueBounds> typeMap) {
    Iterator<Variable> it = arguments.iterator();
    ValueBounds bounds = it.next().updateTypeBounds(typeMap);
    
    while(it.hasNext()) {
      bounds = bounds.union(it.next().updateTypeBounds(typeMap));
    }
    return bounds;
  }

  @Override
  public ValueBounds getValueBounds() {
    throw new UnsupportedOperationException();
  }

  @Override
  public CompiledSexp getCompiledExpr(EmitContext emitContext) {
    throw new UnsupportedOperationException("TODO");
  }

  public List<FlowEdge> getIncomingEdges() {
    return incomingEdges;
  }

  public void setVersionNumber(int argumentIndex, int versionNumber) {
    arguments.set(argumentIndex, arguments.get(argumentIndex).getVersion(versionNumber));
  }

  public Variable getArgument(int j) {
    return arguments.get(j);
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    arguments.set(childIndex, (Variable)child);
  }

  @Override
  public int getChildCount() {
    return arguments.size();
  }

  @Override
  public Expression childAt(int index) {
    return arguments.get(index);
  }
}

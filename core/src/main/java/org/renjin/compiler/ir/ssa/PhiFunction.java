package org.renjin.compiler.ir.ssa;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;
import org.renjin.compiler.cfg.FlowEdge;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.Variable;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PhiFunction implements Expression {

  private List<Variable> arguments;
  private List<FlowEdge> incomingEdges;

  public PhiFunction(Variable variable, Set<FlowEdge> incomingEdges) {
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
  public boolean isDefinitelyPure() {
    return false; // not sure... have to think about this
  }

  @Override
  public int load(EmitContext emitContext, InstructionAdapter mv) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Type getType() {
    throw new UnsupportedOperationException();
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

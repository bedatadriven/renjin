package org.renjin.compiler.ir.ssa;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.objectweb.asm.MethodVisitor;
import org.renjin.compiler.emit.EmitContext;
import org.renjin.compiler.ir.TypeBounds;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.LValue;
import org.renjin.compiler.ir.tac.expressions.Variable;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PhiFunction implements Expression {

  private List<Variable> arguments;
  private Class type;

  public PhiFunction(Variable variable, int count) {
    if(count < 2) {
      throw new IllegalArgumentException("variable=" + variable + ", count=" + count + " (count must be >= 2)");
    }
    this.arguments = Lists.newArrayList();
    for(int i=0;i!=count;++i) {
      arguments.add(variable);
    }
  }

  public PhiFunction(List<Variable> arguments) {
    this.arguments = arguments;
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
  public int emitPush(EmitContext emitContext, MethodVisitor mv) {
    throw new UnsupportedOperationException();
  }

  @Override
  public TypeBounds computeTypeBounds(Map<LValue, TypeBounds> variableMap) {
    Iterator<Variable> it = arguments.iterator();
    TypeBounds bounds = it.next().computeTypeBounds(variableMap);
    
    while(it.hasNext()) {
      bounds = bounds.union(it.next().computeTypeBounds(variableMap));
    }
    return bounds;
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

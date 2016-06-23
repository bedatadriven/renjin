package org.renjin.compiler.ir.ssa;

import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.objectweb.asm.MethodVisitor;
import org.renjin.compiler.emit.EmitContext;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.Variable;


import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

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
  public Class resolveType(VariableMap variableMap) {
    Set<Class> types = Sets.newHashSet();
    for(Variable argument : arguments) {
      if(variableMap.isDefined(argument)) {
        types.add(argument.resolveType(variableMap));
      }
    }
    if(types.size() != 1) {
      throw new UnsupportedOperationException(this + " resolved to zero or multiple types: " + types);
    }
    return (type = types.iterator().next());
  }

  @Override
  public Class getType() {
    Preconditions.checkNotNull(type);

    return type;
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

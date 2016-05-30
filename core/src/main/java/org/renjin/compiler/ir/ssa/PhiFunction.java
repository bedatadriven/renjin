package org.renjin.compiler.ir.ssa;

import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.ExpressionVisitor;
import org.renjin.compiler.ir.tac.expressions.Variable;
import org.renjin.eval.Context;
import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.Sets;
import org.renjin.sexp.SEXP;

import java.util.List;
import java.util.Set;

public class PhiFunction implements Expression {

  private List<Variable> arguments;
  
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

  @Override
  public Object retrieveValue(Context context, Object[] temps) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<Variable> variables() {
    return Sets.newHashSet(arguments);
  }

  @Override
  public String toString() {
    return "Φ(" + Joiner.on(", ").join(arguments) + ")";
  }

  @Override
  public Expression replaceVariable(Variable name, Variable newName) {
    List<Variable> newArguments = Lists.newArrayList();
    for(Variable arg : arguments) {
      newArguments.add(arg.equals(name) ? newName : arg);
    }
    return new PhiFunction(newArguments);
  }
  
  public PhiFunction replaceVariable(int j, int i) {
    List<Variable> newArguments = Lists.newArrayList(arguments);
    newArguments.set(j, new SsaVariable(newArguments.get(j), i));
    return new PhiFunction(newArguments);
  }

  public Variable getArgument(int j) {
    return arguments.get(j);
  }

  @Override
  public List<Expression> getChildren() {
    return (List)arguments;
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    arguments.set(childIndex, (Variable)child);
  }

  @Override
  public void accept(ExpressionVisitor visitor) {
    visitor.visitPhiFunction(this);
  }

  @Override
  public SEXP getSExpression() {
    return arguments.get(0).getSExpression();
  }
  
  
} 

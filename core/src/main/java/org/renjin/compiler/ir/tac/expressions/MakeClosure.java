package org.renjin.compiler.ir.tac.expressions;


import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.renjin.compiler.ir.tac.IRFunction;
import org.renjin.eval.Context;
import org.renjin.sexp.SEXP;


public class MakeClosure implements Expression {

  private IRFunction function;
  
  public MakeClosure(IRFunction function) {
    this.function = function;
  }

  @Override
  public Object retrieveValue(Context context, Object[] temps) {
    return function.newClosure(context);
  }

  @Override
  public Set<Variable> variables() {
    return Collections.emptySet();
  }

  @Override
  public Expression replaceVariable(Variable name, Variable newName) {
    return this;
  }
  
  public IRFunction getFunction() {
    return function;
  }

  @Override
  public List<Expression> getChildren() {
    return Collections.emptyList();
  }
  
  @Override
  public void setChild(int i, Expression expr) {
    throw new IllegalArgumentException();
  }

  @Override
  public String toString() {
    return "closure(" + function.toString() + ")";
  }

  @Override
  public void accept(ExpressionVisitor visitor) {
    visitor.visitMakeClosure(this);
  }

  @Override
  public SEXP getSExpression() {
    throw new UnsupportedOperationException();
  }
}

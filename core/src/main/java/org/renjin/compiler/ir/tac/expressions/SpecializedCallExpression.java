package org.renjin.compiler.ir.tac.expressions;

import org.renjin.compiler.ir.ssa.VariableMap;
import org.renjin.sexp.AtomicVector;

public abstract class SpecializedCallExpression implements Expression {
  protected final Expression[] arguments;

  public SpecializedCallExpression(Expression... arguments) {
    this.arguments = arguments;
  }

  @Override
  public final void setChild(int childIndex, Expression child) {
    arguments[childIndex] = child;
  }

  @Override
  public final int getChildCount() {
    return arguments.length;
  }

  @Override
  public final Expression childAt(int index) {
    return arguments[index];
  }
  
  public abstract boolean isFunctionDefinitelyPure();

  @Override
  public boolean isDefinitelyPure() {
    if(!isFunctionDefinitelyPure()) {
      return false;
    }
    for(int i=0;i!=arguments.length;++i) {
      if(!arguments[i].isDefinitelyPure()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public Class resolveType(VariableMap variableMap) {
    return AtomicVector.class;
  }

}

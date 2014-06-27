package org.renjin.compiler.ir.tac.expressions;

import org.renjin.compiler.ir.ssa.VariableMap;


/**
 * A value known at compile time.
 */
public abstract class Constant implements SimpleExpression {

  public abstract Object getValue();

  @Override
  public final int getChildCount() {
    return 0;
  }

  @Override
  public final Expression childAt(int index) {
    throw new IllegalArgumentException();
  }

  @Override
  public final boolean isDefinitelyPure() {
    return true;
  }

  @Override
  public final void setChild(int i, Expression expr) {
    throw new IllegalArgumentException();
  }

  @Override
  public Class getType() {
    return computeType();
  }

  @Override
  public Class resolveType(VariableMap variableMap) {
    return computeType();
  }

  private Class computeType() {
    Class valueClass = getValue().getClass();
    if(valueClass.equals(Double.class)) {
      return double.class;
    } else if(valueClass.equals(Integer.class)) {
      return int.class;
    }
    return valueClass;
  }
}

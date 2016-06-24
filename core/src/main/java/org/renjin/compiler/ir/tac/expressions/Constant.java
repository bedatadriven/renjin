package org.renjin.compiler.ir.tac.expressions;

import org.renjin.compiler.ir.TypeBounds;

import java.util.Map;


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
  public TypeBounds computeTypeBounds(Map<LValue, TypeBounds> variableMap) {
    return computeType();
  }

  private TypeBounds computeType() {
    Class valueClass = getValue().getClass();
    if(valueClass.equals(Double.class)) {
      return TypeBounds.scalarDouble();
    } else if(valueClass.equals(Integer.class)) {
      return TypeBounds.scalarInt();
    }
    return TypeBounds.openSet();
  }
}

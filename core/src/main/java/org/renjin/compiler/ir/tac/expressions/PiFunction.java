package org.renjin.compiler.ir.tac.expressions;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;

/**
 *
 */
public class PiFunction implements Expression {

  private Expression variable;
  private Expression function;

  private ValueBounds bounds = ValueBounds.UNBOUNDED;

  public PiFunction(Expression function, Expression variable) {
    this.function = function;
    this.variable = variable;
  }

  @Override
  public boolean isPure() {
    return true;
  }

  @Override
  public ValueBounds updateTypeBounds(ValueBoundsMap typeMap) {

    ValueBounds variableBounds = variable.updateTypeBounds(typeMap);
    ValueBounds functionBounds = function.updateTypeBounds(typeMap);

    if(variableBounds.getTypeSet() == TypeSet.FUNCTION) {
      // If the variable is DEFINITELY a function, then we know that x* will
      // take that value
      bounds = variableBounds;

    } else if (!TypeSet.mightBe(variableBounds.getTypeSet(), TypeSet.FUNCTION)) {
      // If the variable is definitely NOT a function, then then x* will retain
      // the value of x*
      bounds = functionBounds;

    } else {
      // Otherwise, it's indeterminate.

      bounds = functionBounds.union(variableBounds);
    }
    return bounds;
  }

  @Override
  public ValueBounds getValueBounds() {
    return bounds;
  }

  @Override
  public CompiledSexp getCompiledExpr(EmitContext emitContext) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getChildCount() {
    return 2;
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    switch (childIndex) {
      case 0:
        this.variable = child;
        break;
      case 1:
        this.function = child;
        break;
      default:
        throw new IllegalArgumentException();
    }
  }

  @Override
  public Expression childAt(int index) {
    switch (index) {
      case 0:
        return variable;
      case 1:
        return function;
      default:
        throw new IllegalArgumentException("i:" + index);
    }
  }

  @Override
  public String toString() {
    return "π(" + variable + ", " + function + ")";
  }
}

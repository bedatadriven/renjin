package org.renjin.primitives.summary;

import org.renjin.primitives.vector.MemoizedComputation;
import org.renjin.sexp.*;


public abstract class DeferredSummary extends DoubleVector implements MemoizedComputation {
  protected final Vector vector;
  private double result;
  private boolean calculated = false;

  public DeferredSummary(Vector vector, AttributeMap attributes) {
    super(attributes);
    this.vector = vector;
  }

  @Override
  public final Vector[] getOperands() {
    return new Vector[]  { vector };
  }

  @Override
  public final double getElementAsDouble(int index) {
    if(index != 0) {
      throw new IllegalArgumentException("index: " + index);
    }
    if(!calculated) {
      result = calculate();
    }
    return result;
  }

  protected abstract double calculate();

  @Override
  public final int length() {
    return 1;
  }

  @Override
  public final boolean isConstantAccessTime() {
    return false;
  }

  @Override
  public final boolean isCalculated() {
    return calculated;
  }

  @Override
  public final Vector forceResult() {
    if(!calculated) {
      result = calculate();
    }
    return new DoubleArrayVector(result);
  }

  @Override
  public final void setResult(Vector result) {
    this.result = result.getElementAsDouble(0);
    this.calculated = true;
  }
}

package org.renjin.primitives.vector;

import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

public abstract class MemoizedDoubleVector extends DoubleVector implements MemoizedComputation {

  private final Vector[] operands;
  private final int length;
  private Vector result;


  public MemoizedDoubleVector(Vector[] operands, int length, AttributeMap attributes) {
    super(attributes);
    this.operands = operands;
    this.length = length;
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new AttributeDecoratingVector(this, attributes);
  }

  @Override
  public double getElementAsDouble(int index) {
    if(result == null) {
      forceResult();
    }
    return result.getElementAsDouble(index);
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  public void setResult(Vector result) {
    this.result = result;
  }

  @Override
  public boolean isCalculated() {
    return result != null;
  }

  @Override
  public Vector[] getOperands() {
    return operands;
  }
}

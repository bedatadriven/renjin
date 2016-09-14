package org.renjin.primitives.vector;

import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

/**
 * Created by alex on 14-9-16.
 */
public class MemoizedIntVector extends IntVector implements MemoizedComputation {
  @Override
  public Vector forceResult() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setResult(Vector result) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isCalculated() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Vector[] getOperands() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getComputationName() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int length() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getElementAsInt(int i) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isConstantAccessTime() {
    throw new UnsupportedOperationException();
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    throw new UnsupportedOperationException();
  }
}

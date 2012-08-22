package org.renjin.primitives.vector;

import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.SEXP;

public class ConstantIntVector extends IntVector {

  private int value;
  private int length;

  public ConstantIntVector(int value, int length, AttributeMap attributes) {
    super(attributes);
    this.value = value;
    this.length = length;
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  public int getElementAsInt(int i) {
    return value;
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new ConstantIntVector(value, length, attributes);
  }
}

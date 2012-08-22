package org.renjin.primitives.vector;

import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.SEXP;


public class ConstantDoubleVector extends DoubleVector {

  private double value;
  private int length;

  public ConstantDoubleVector(double value, int length) {
    this.value = value;
    this.length = length;
  }

  public ConstantDoubleVector(double value, int length, AttributeMap attributes) {
    super(attributes);
    this.value = value;
    this.length = length;
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new ConstantDoubleVector(value, length, attributes);
  }

  @Override
  public double getElementAsDouble(int index) {
    return value;
  }

  @Override
  public int length() {
    return length;
  }
}

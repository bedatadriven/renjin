package org.renjin.primitives.vector;

import org.apache.commons.math.complex.Complex;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.ComplexVector;
import org.renjin.sexp.Vector;

public class ConvertingComplexVector extends ComplexVector {
  private Vector x;

  public ConvertingComplexVector(Vector x, AttributeMap attributes) {
    super(attributes);
    this.x = x;
  }

  @Override
  public int length() {
    return x.length();
  }

  @Override
  public Complex getElementAsComplex(int index) {
    return x.getElementAsComplex(index);
  }

  @Override
  public boolean isConstantAccessTime() {
    return x.isConstantAccessTime();
  }

}

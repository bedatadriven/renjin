package org.renjin.primitives.vector;

import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.ComplexVector;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.SEXP;

/**
 * Lazy double vector that computes the imaginary part of each element 
 * in a complex vector.
 */
public class ImaginaryVector extends DoubleVector {

  private ComplexVector vector;

  public ImaginaryVector(ComplexVector vector, AttributeMap attributes) {
    super(attributes);
    this.vector = vector;
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new ImaginaryVector(vector, attributes);
  }

  @Override
  public double getElementAsDouble(int index) {
    return vector.getElementAsComplexIm(index);
  }

  @Override
  public boolean isConstantAccessTime() {
    return vector.isConstantAccessTime();
  }

  @Override
  public int length() {
    return vector.length();
  }
}

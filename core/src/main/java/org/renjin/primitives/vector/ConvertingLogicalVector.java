package org.renjin.primitives.vector;

import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.LogicalVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;


public class ConvertingLogicalVector extends LogicalVector {

  private final Vector x;

  public ConvertingLogicalVector(Vector x, AttributeMap attributes) {
    super(attributes);
    this.x = x;
  }

  @Override
  public int length() {
    return x.length();
  }

  @Override
  public int getElementAsRawLogical(int index) {
    return x.getElementAsRawLogical(index);
  }

  @Override
  public boolean isConstantAccessTime() {
    return x.isConstantAccessTime();
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new ConvertingLogicalVector(x, attributes);
  }
}

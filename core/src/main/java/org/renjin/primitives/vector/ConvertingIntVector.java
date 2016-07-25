package org.renjin.primitives.vector;

import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

public class ConvertingIntVector extends IntVector {
  private Vector x;

  public ConvertingIntVector(Vector x, AttributeMap attributes) {
    this.x = x;
  }

  @Override
  public int length() {
    return x.length();
  }

  @Override
  public int getElementAsInt(int i) {
    return x.getElementAsInt(i);
  }

  @Override
  public boolean isConstantAccessTime() {
    return x.isConstantAccessTime();
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new ConvertingIntVector(x, attributes);
  }
}

package org.renjin.primitives.vector;

import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;


public class CyclingIntVector extends IntVector {
  private final Vector source;
  private final int sourceLength;
  private final int length;

  public CyclingIntVector(Vector source, int length) {
    this(source, length, AttributeMap.EMPTY);
  }

  public CyclingIntVector(Vector source, int length, AttributeMap attributes) {
    super(attributes);
    this.source = source;
    this.sourceLength = source.length();
    this.length = length;
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  public int getElementAsInt(int i) {
    return getElementAsInt(i % sourceLength);
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new CyclingIntVector(source, length, attributes);
  }
}

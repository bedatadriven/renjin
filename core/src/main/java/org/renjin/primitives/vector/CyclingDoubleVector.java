package org.renjin.primitives.vector;


import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

public class CyclingDoubleVector extends DoubleVector {
  private final Vector source;
  private int sourceLength;
  private final int length;

  public CyclingDoubleVector(Vector source, int length, AttributeMap attributes) {
    super(attributes);
    this.source = source;
    this.sourceLength = source.length();
    this.length = length;
  }

  public CyclingDoubleVector(Vector source, int length) {
    this(source, length, AttributeMap.EMPTY);
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new CyclingDoubleVector(source, length, attributes);
  }

  @Override
  public double getElementAsDouble(int index) {
    sourceLength = source.length();
    return getElementAsInt(index % sourceLength);
  }

  @Override
  public int length() {
    return sourceLength;
  }
}

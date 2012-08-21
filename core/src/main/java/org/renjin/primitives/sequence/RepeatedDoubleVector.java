package org.renjin.primitives.sequence;


import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;

public class RepeatedDoubleVector extends DoubleVector {
  private final DoubleVector source;
  private int length;

  public RepeatedDoubleVector(DoubleVector source, int length, PairList attributes) {
    super(attributes);
    this.source = source;
    this.length = length;
  }

  @Override
  protected SEXP cloneWithNewAttributes(PairList attributes) {
    return new RepeatedDoubleVector(source, length, attributes);
  }

  @Override
  public double getElementAsDouble(int index) {
    return source.getElementAsDouble(index % source.length());
  }

  @Override
  public int length() {
    return length;
  }
}

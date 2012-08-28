package org.renjin.primitives.sequence;


import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

/**
 */
public class DoubleSequence extends DoubleVector {

  private double from;
  private double by;
  private int length;

  public DoubleSequence(AttributeMap attributes, double from, double by, int length) {
    super(attributes);
    this.from = from;
    this.by = by;
    this.length = length;
  }

  public DoubleSequence(double from, double by, int length) {
    this.from = from;
    this.by = by;
    this.length = length;
  }

  @Override
  public double getElementAsDouble(int index) {
    return from + index * by;
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new DoubleSequence(attributes, from, by, length);
  }

  public static Vector fromTo(double n1, double n2) {
    if(n1 <= n2) {
      return new DoubleSequence(n1, 1d, (int)Math.ceil(n2-n1));
    } else {
      return new DoubleSequence(n1, -1d, (int)(Math.floor(n1-n2)+1));
    }
  }
}

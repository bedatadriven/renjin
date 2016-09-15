package org.renjin.primitives.sequence;

import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

public class RepIntVector extends IntVector {

  public static final int LENGTH_THRESHOLD = 100;

  private final Vector source;
  private int length;
  private int each;

  public RepIntVector(Vector source, int length, int each, AttributeMap attributes) {
    super(attributes);
    this.source = source;
    this.length = length;
    this.each = each;
    if(this.length <= 0) {
      throw new IllegalArgumentException("length: " + length);
    }
  }
  
  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new RepIntVector(source, length, each, attributes);
  }

  @Override
  public int getElementAsInt(int index) {
    return source.getElementAsInt((index / each) % source.length());
  }

  @Override
  public boolean isConstantAccessTime() {
    return true;
  }

  @Override
  public int length() {
    return length;
  }
}

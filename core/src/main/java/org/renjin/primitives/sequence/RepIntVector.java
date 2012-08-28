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

  public RepIntVector(Vector source, int length, int each) {
    this(source, length, each, transformAttributes(source, length, each));
  }

  private static AttributeMap transformAttributes(Vector source, int length, int each) {
    if(source.getAttributes().hasNames()) {
      return source.getAttributes()
              .copy()
              .setNames(new RepStringVector(source.getAttributes().getNames(), length, each, AttributeMap.EMPTY))
              .build();
    } else {
      return source.getAttributes();
    }
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new RepDoubleVector(source, length, each, attributes);
  }

  @Override
  public int getElementAsInt(int index) {
    return source.getElementAsInt((index / each) % source.length());
  }

  @Override
  public int length() {
    return length;
  }
}

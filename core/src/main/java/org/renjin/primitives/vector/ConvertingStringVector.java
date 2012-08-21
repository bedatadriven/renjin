package org.renjin.primitives.vector;

import org.renjin.sexp.Null;
import org.renjin.sexp.PairList;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Vector;

public class ConvertingStringVector extends StringVector {

  private final Vector source;

  public ConvertingStringVector(Vector source, PairList attributes) {
    super(attributes);
    this.source = source;
  }

  public ConvertingStringVector(Vector source) {
    this(source, Null.INSTANCE);
  }

  @Override
  public String getElementAsString(int index) {
    return source.getElementAsString(index);
  }

  @Override
  public int length() {
    return source.length();
  }

  @Override
  protected StringVector cloneWithNewAttributes(PairList attributes) {
    return new ConvertingStringVector(source, attributes);
  }
}

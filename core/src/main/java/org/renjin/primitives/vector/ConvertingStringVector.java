package org.renjin.primitives.vector;

import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Vector;

public class ConvertingStringVector extends StringVector {

  private final Vector source;

  public ConvertingStringVector(Vector source, AttributeMap attributes) {
    super(attributes);
    this.source = source;
  }

  public ConvertingStringVector(Vector source) {
    this(source, AttributeMap.EMPTY);
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
  protected StringVector cloneWithNewAttributes(AttributeMap attributes) {
    return new ConvertingStringVector(source, attributes);
  }
}

package org.renjin.primitives.sequence;


import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Vector;

public class RepStringVector extends StringVector {

  public Vector source;
  private int sourceLength;
  private int length;
  private int each;

  public RepStringVector(Vector source, int length, int each, AttributeMap attributes) {
    super(attributes);
    this.source = source;
    this.sourceLength = source.length();
    this.length = length;
    this.each = each;
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  protected StringVector cloneWithNewAttributes(AttributeMap attributes) {
    return new RepStringVector(source, length, each, attributes);
  }

  @Override
  public String getElementAsString(int index) {
    sourceLength = source.length();
    return source.getElementAsString( (index / each) % sourceLength);
  }
}

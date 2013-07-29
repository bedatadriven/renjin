package org.renjin.primitives.vector;


import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.StringVector;

public class ConstantStringVector extends StringVector {

  private String value;
  private int length;

  public ConstantStringVector(String value, int length, AttributeMap attributes) {
    super(attributes);
    this.value = value;
    this.length = length;
  }

  public ConstantStringVector(String value, int length) {
    this(value, length, AttributeMap.EMPTY);
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  protected StringVector cloneWithNewAttributes(AttributeMap attributes) {
    return new ConstantStringVector(value, length, attributes);
  }

  @Override
  public String getElementAsString(int index) {
    return value;
  }

  @Override
  public boolean isConstantAccessTime() {
    return true;
  }
}

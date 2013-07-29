package org.renjin.primitives.vector;


import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Vector;

public class PrefixedStringVector extends StringVector {

  private String prefix;
  private Vector vector;

  public PrefixedStringVector(String prefix, Vector vector, AttributeMap attributes) {
    super(attributes);
    this.prefix = prefix;
    this.vector = vector;
  }

  @Override
  public int length() {
    return vector.length();
  }

  @Override
  protected StringVector cloneWithNewAttributes(AttributeMap attributes) {
    return new PrefixedStringVector(prefix, vector, attributes);
  }

  @Override
  public String getElementAsString(int index) {
    String value = vector.getElementAsString(index);
    if(value == null) {
      return null;
    } else {
      return prefix + value;
    }
  }

  @Override
  public boolean isConstantAccessTime() {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }
}

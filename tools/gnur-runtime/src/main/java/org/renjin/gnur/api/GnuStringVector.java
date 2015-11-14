package org.renjin.gnur.api;

import org.renjin.gcc.runtime.BytePtr;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.StringVector;

/**
 * StringVector implementation backed by BytePtrs
 */
public class GnuStringVector extends StringVector {
  
  private BytePtr[] values;

  public GnuStringVector(BytePtr[] values) {
    this(values, AttributeMap.EMPTY);
  }
  
  public GnuStringVector(BytePtr[] values, AttributeMap attributes) {
    super(attributes);
    this.values = values;
  }

  @Override
  public int length() {
    return values.length;
  }

  @Override
  protected StringVector cloneWithNewAttributes(AttributeMap attributes) {
    return new GnuStringVector(values, attributes);
  }

  @Override
  public String getElementAsString(int index) {
    BytePtr value = values[index];
    if(value == null) {
      return null;
    } else {
      return value.nullTerminatedString();
    }
  }

  @Override
  public boolean isElementNA(int index) {
    return values[index] == null;
  }

  @Override
  public boolean isConstantAccessTime() {
    return true;
  }

  public void set(int index, GnuCharSexp charValue) {
    values[index] = charValue.getValue();
  }
}

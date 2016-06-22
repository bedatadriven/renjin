package org.renjin.primitives;

import org.renjin.gcc.runtime.BytePtr;
import org.renjin.gcc.runtime.ObjectPtr;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.StringVector;

public class NativeStringVector extends StringVector {
  
  private BytePtr[] array;
  private int offset;
  private int length;

  public NativeStringVector(BytePtr[] array, int offset, int length, AttributeMap attributes) {
    super(attributes);
    this.array = array;
    this.offset = offset;
    this.length = length;
  }

  public NativeStringVector(ObjectPtr ptr, AttributeMap attributes) {
    super(attributes);
    this.array = (BytePtr[])ptr.array;
    this.offset = ptr.offset;
    this.length = ptr.array.length;
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  protected StringVector cloneWithNewAttributes(AttributeMap attributes) {
    return new NativeStringVector(array, offset, length, attributes);
  }

  @Override
  public String getElementAsString(int index) {
    BytePtr string = array[offset + index];
    if(string == null) {
      return null;
    } else {
      return string.nullTerminatedString();
    }
  }

  @Override
  public boolean isConstantAccessTime() {
    return true;
  }
}

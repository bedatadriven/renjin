package org.renjin.gcc.runtime;


public class BooleanPtr implements Ptr {
  public final boolean[] array;
  public final int offset;

  public BooleanPtr(boolean[] array, int offset) {
    this.array = array;
    this.offset = offset;
  }

  public BooleanPtr(boolean... values) {
    this.array = values;
    this.offset = 0;
  }

  public boolean unwrap() {
    return array[offset];
  }

  @Override
  public boolean[] getArray() {
    return array;
  }

  @Override
  public int getOffset() {
    return offset;
  }
}

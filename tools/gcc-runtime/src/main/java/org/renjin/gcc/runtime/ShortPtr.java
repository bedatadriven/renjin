package org.renjin.gcc.runtime;

import java.util.Arrays;


public class ShortPtr implements Ptr {
  public short[] array;
  public int offset;

  public ShortPtr(short[] array, int offset) {
    this.array = array;
    this.offset = offset;
  }

  public ShortPtr(short... array) {
    this.array = array;
    this.offset = 0;
  }

  public short unwrap() {
    return array[offset];
  }

  @Override
  public String toString() {
    return offset + "+" + Arrays.toString(array);
  }
}

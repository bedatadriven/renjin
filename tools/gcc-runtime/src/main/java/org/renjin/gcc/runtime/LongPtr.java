package org.renjin.gcc.runtime;


import java.util.Arrays;

public class LongPtr implements Ptr {
  public final long[] array;
  public final int offset;

  public LongPtr(long[] array, int offset) {
    this.array = array;
    this.offset = offset;
  }

  public LongPtr(long... array) {
    this.array = array;
    this.offset = 0;
  }

  public long unwrap() {
    return array[offset];
  }

  @Override
  public String toString() {
    return offset + "+" + Arrays.toString(array);
  }
}

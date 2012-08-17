package org.renjin.gcc.runtime;


import java.util.Arrays;

public class IntPtr implements Ptr {
  public final int[] array;
  public final int offset;

  public IntPtr(int[] array, int offset) {
    this.array = array;
    this.offset = offset;
  }

  public IntPtr(int... array) {
    this.array = array;
    this.offset = 0;
  }

  @Override
  public String toString() {
    return offset + "+" + Arrays.toString(array);
  }
}

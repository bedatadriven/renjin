package org.renjin.gcc.runtime;


import java.util.Arrays;

public class IntPtr implements Ptr {
  public int[] array;
  public int offset;

  public IntPtr(int[] array, int offset) {
    this.array = array;
    this.offset = offset;
  }

  public IntPtr(int... array) {
    this.array = array;
    this.offset = 0;
  }

  public int unwrap() {
    return array[offset];
  }
  
  public void update(int[] array, int offset) {
    this.array = array;
    this.offset = offset;
  }

  @Override
  public String toString() {
    return offset + "+" + Arrays.toString(array);
  }
}

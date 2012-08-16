package org.renjin.gcc.runtime;


public class IntPtr implements Ptr {
  public final int[] array;
  public final int offset;

  public IntPtr(int[] array, int offset) {
    this.array = array;
    this.offset = offset;
  }

}

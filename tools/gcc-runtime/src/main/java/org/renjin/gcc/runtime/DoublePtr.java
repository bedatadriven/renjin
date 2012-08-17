package org.renjin.gcc.runtime;


import java.util.Arrays;

public class DoublePtr implements Ptr {
  public final double[] array;
  public final int offset;

  public DoublePtr(double[] array, int offset) {
    this.array = array;
    this.offset = offset;
  }

  public DoublePtr(double... values) {
    this.array = values;
    this.offset = 0;
  }

  @Override
  public String toString() {
    return offset + "+" + Arrays.toString(array);
  }
}

package org.renjin.gcc.runtime;


import java.util.Arrays;

public class DoublePtr implements Ptr {
  public double[] array;
  public int offset;

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

  public double unwrap() {
    return array[offset];
  }

  public double get(int i) {
    return array[offset+i];
  }

  public void set(int index, double value) {
    array[offset+index] = value;
  }
  
  public void update(double[] array,  int offset) {
    this.array = array;
    this.offset = offset;
  }
}

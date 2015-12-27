package org.renjin.gcc.runtime;


import java.util.Arrays;

public class FloatPtr implements Ptr {

  public float[] array;
  public int offset;

  public FloatPtr(float[] array, int offset) {
    this.array = array;
    this.offset = offset;
  }

  public FloatPtr(float... values) {
    this.array = values;
    this.offset = 0;
  }

  @Override
  public String toString() {
    return offset + "+" + Arrays.toString(array);
  }

  public float unwrap() {
    return array[offset];
  }

  public float get(int i) {
    return array[offset+i];
  }

  public void set(int index, float value) {
    array[offset+index] = value;
  }

  public void update(float[] array,  int offset) {
    this.array = array;
    this.offset = offset;
  }
}

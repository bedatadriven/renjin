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
  public float[] getArray() {
    return array;
  }

  @Override
  public int getOffset() {
    return offset;
  }

  @Override
  public Ptr realloc(int newSizeInBytes) {
    return new FloatPtr(Realloc.realloc(array, offset, newSizeInBytes / 4));
  }

  @Override
  public Ptr pointerPlus(int bytes) {
    return new FloatPtr(array, offset + (bytes / 4));
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
  
  public static FloatPtr cast(Object voidPointer) {
    if(voidPointer instanceof MallocThunk) {
      return ((MallocThunk) voidPointer).floatPtr();
    }
    return (FloatPtr) voidPointer;
  }
}

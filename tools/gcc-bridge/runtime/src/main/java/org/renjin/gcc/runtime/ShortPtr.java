package org.renjin.gcc.runtime;

import java.util.Arrays;


public class ShortPtr implements Ptr {
  
  public static final ShortPtr NULL = new ShortPtr();
  
  public final short[] array;
  public final int offset;
  
  private ShortPtr() {
    this.array = null;
    this.offset = 0;
  }

  public ShortPtr(short[] array, int offset) {
    this.array = array;
    this.offset = offset;
  }

  public ShortPtr(short... array) {
    this.array = array;
    this.offset = 0;
  }

  @Override
  public short[] getArray() {
    return array;
  }

  @Override
  public int getOffset() {
    return offset;
  }

  @Override
  public Ptr realloc(int newSizeInBytes) {
    return new ShortPtr(Realloc.realloc(array, offset, newSizeInBytes / 2));
  }

  @Override
  public Ptr pointerPlus(int bytes) {
    return new ShortPtr(array, offset + (bytes / 2));
  }

  public short unwrap() {
    return array[offset];
  }

  @Override
  public String toString() {
    return offset + "+" + Arrays.toString(array);
  }


  public static void memset(short[] array, int offset, int value, int length) {
    throw new UnsupportedOperationException("TODO");
  }

  public static short memset(int byteValue) {
    throw new UnsupportedOperationException("TODO");
  }

  public static ShortPtr cast(Object voidPointer) {
    if(voidPointer instanceof MallocThunk) {
      return ((MallocThunk) voidPointer).shortPtr();
    }
    return (ShortPtr) voidPointer;  
  }
}

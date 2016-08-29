package org.renjin.gcc.runtime;


public class BooleanPtr implements Ptr {

  public static final BooleanPtr NULL = new BooleanPtr();

  public final boolean[] array;
  public final int offset;

  private BooleanPtr() {
    array = null;
    offset = 0;
  }
  
  public BooleanPtr(boolean[] array, int offset) {
    this.array = array;
    this.offset = offset;
  }

  public BooleanPtr(boolean... values) {
    this.array = values;
    this.offset = 0;
  }

  public boolean unwrap() {
    return array[offset];
  }

  @Override
  public boolean[] getArray() {
    return array;
  }

  @Override
  public int getOffset() {
    return offset;
  }

  @Override
  public BooleanPtr realloc(int newSizeInBytes) {
    return new BooleanPtr(Realloc.realloc(array, offset, newSizeInBytes));
  }

  @Override
  public Ptr pointerPlus(int bytes) {
    return new BooleanPtr(array, offset + bytes);
  }

  public static BooleanPtr cast(Object voidPointer) {
    if(voidPointer instanceof MallocThunk) {
      return ((MallocThunk) voidPointer).booleanPtr();
    }
    return (BooleanPtr) voidPointer;
  }
}

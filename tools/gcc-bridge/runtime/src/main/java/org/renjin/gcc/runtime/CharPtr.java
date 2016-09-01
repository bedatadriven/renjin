package org.renjin.gcc.runtime;

import java.util.Arrays;

public class CharPtr implements Ptr {
  
  public static final CharPtr NULL = new CharPtr();
  
  public final char[] array;
  public final int offset;
  
  private CharPtr() {
    this.array = null;
    this.offset = 0;
  }

  public CharPtr(char[] array, int offset) {
    this.array = array;
    this.offset = offset;
  }

  public CharPtr(char... array) {
    this.array = array;
    this.offset = 0;
  }

  @Override
  public char[] getArray() {
    return array;
  }

  @Override
  public int getOffset() {
    return offset;
  }

  @Override
  public CharPtr realloc(int newSizeInBytes) {
    return new CharPtr(Realloc.realloc(array, offset, newSizeInBytes / 2));
  }

  @Override
  public Ptr pointerPlus(int bytes) {
    return new CharPtr(array, offset + (bytes / 2));
  }

  public static CharPtr fromString(String string) {
    int nchars = string.length();
    char array[] = new char[nchars+1];
    System.arraycopy(string.toCharArray(), 0, array, 0, nchars);
    return new CharPtr(array);
  }

  @Override
  public String toString() {
    return offset + "+" + Arrays.toString(array);
  }

  public String asString() {
    // look for null terminator
    int length;
    for(length=offset;length<array.length;++length) {
      if(array[length] == 0) {
        break;
      }
    }
    return new String(array, offset, length-offset);
  }
  
  public static CharPtr cast(Object voidPointer) {
    if(voidPointer instanceof MallocThunk) {
      return ((MallocThunk) voidPointer).charPtr();
    } 
    return (CharPtr) voidPointer;
  }

  public static void memset(char[] array, int offset, int value, int length) {
    throw new UnsupportedOperationException("TODO");
  }
  
  public static char memset(int byteValue) {
    throw new UnsupportedOperationException("TODO");
  }
}
package org.renjin.gcc.runtime;

import java.util.Arrays;

public class CharPtr implements Ptr {
  public char[] array;
  public int offset;

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
}
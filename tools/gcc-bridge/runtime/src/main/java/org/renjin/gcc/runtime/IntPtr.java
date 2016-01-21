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
  
  public void set(int value) {
    array[offset] = value;
  }
  
  public void update(int[] array, int offset) {
    this.array = array;
    this.offset = offset;
  }

  @Override
  public String toString() {
    return offset + "+" + Arrays.toString(array);
  }

  /**
   * Copies the character c (an unsigned char) to 
   * the first n characters of the string pointed to, by the argument str.
   *
   * @param str an array of doubles
   * @param strOffset the first element to set
   * @param c the byte value to set
   * @param n the number of bytes to set
   */
  public static void memset(int[] str, int strOffset, int byteValue, int n) {
    
    assert n % Integer.SIZE == 0;
    
    int value = byteValue << 24 | 
        (byteValue & 0xFF) << 16 | 
        (byteValue & 0xFF) << 8 | 
        (byteValue & 0xFF);
    
    Arrays.fill(str, strOffset, strOffset + (n / Integer.SIZE), value);
  }
  
}

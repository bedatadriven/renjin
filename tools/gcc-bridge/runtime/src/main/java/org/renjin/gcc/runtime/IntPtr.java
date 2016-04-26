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
  
  public static int memcmp(IntPtr x, IntPtr y, int n) {
    return memcmp(x.array, x.offset, y.array, y.offset, n);
  }

  public static int memcmp(int[] x, int xi, int[] y, int yi, int n) {
    while(n > 0) {
      int vx = x[xi];
      int vy = y[yi];

      if(vx != vy || n < 4) {
        return memcmp(vx, vy, n);
      }
      xi++;
      yi++;
      n-= 4;
    }
    return 0;
  }


  /**
   * Compares the given integer values byte for byte. If (n >= 4), all 4 bytes
   * of the int values are compared.
   *
   * @param x the first int value
   * @param y the second int value
   * @param n the number of <strong>bytes</strong> to compare. 
   * @return 0 if the first {@code n} bytes of the long values are equal, -1 if the first is less than the second,
   * or +1 if the first is greater than the second.
   */
  public static int memcmp(int x, int y, int n) {
    for (int i = 0; i < n; ++i) {
      int xb = (int) (x & 0xffL);
      int yb = (int) (y & 0xffL);

      if (xb < yb) {
        return -1;
      } else if (xb > yb) {
        return 1;
      }
      x >>= 8;
      y >>= 8;
    }
    return 0;
  }

}

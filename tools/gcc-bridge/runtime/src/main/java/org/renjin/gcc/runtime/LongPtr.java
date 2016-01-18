package org.renjin.gcc.runtime;


import java.util.Arrays;

public class LongPtr implements Ptr {
  public long[] array;
  public int offset;

  public LongPtr(long[] array, int offset) {
    this.array = array;
    this.offset = offset;
  }

  public LongPtr(long... array) {
    this.array = array;
    this.offset = 0;
  }

  public long unwrap() {
    return array[offset];
  }

  public void update(long[] array, int offset) {
    this.array = array;
    this.offset = offset;
  }
  
  @Override
  public String toString() {
    return offset + "+" + Arrays.toString(array);
  }
  
  public static int memcmp(long[] x, int xi, long[] y, int yi, int n) {
    while(n > 0) {
      long vx = x[xi];
      long vy = y[yi];
      
      if(vx != vy || n < 8) {
        return memcmp(vx, vy, n);
      }
      xi++;
      yi++;
      n-= 8;
    }
    return 0;
  }


  /**
   * Compares the given long values byte for byte. If (n >= 8), all 8 bytes
   * of the long values are compared.
   * 
   * @param x the first long value
   * @param y the second long value
   * @param n the number of <strong>bytes</strong> to compare. 
   * @return 0 if the first {@code n} bytes of the long values are equal, -1 if the first is less than the second,
   * or +1 if the first is greater than the second.
   */
  public static int memcmp(long x, long y, int n) {
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

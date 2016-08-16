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

  @Override
  public long[] getArray() {
    return array;
  }

  @Override
  public int getOffset() {
    return offset;
  }

  @Override
  public LongPtr realloc(int newSizeInBytes) {
    return new LongPtr(Realloc.realloc(array, offset, newSizeInBytes / 8));
  }

  @Override
  public Ptr pointerPlus(int bytes) {
    return new LongPtr(array, offset + (bytes / 8));
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
  
  
  public static int memcmp(LongPtr x, LongPtr y, int n) {
    return memcmp(x.array, x.offset, y.array, y.offset, n);
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

  /**
   * Copies the character c (an unsigned char) to 
   * the first n characters of the string pointed to, by the argument str.
   *
   * @param str an array of doubles
   * @param strOffset the first element to set
   * @param c the byte value to set
   * @param n the number of bytes to set
   */
  public static void memset(double[] str, int strOffset, int c, int n) {

    assert n % Long.SIZE == 0;

    long longValue =
        (c & 0xFFL) << 56
            | (c & 0xFFL) << 48
            | (c & 0xFFL) << 40
            | (c & 0xFFL) << 32
            | (c & 0xFFL) << 24
            | (c & 0xFFL) << 16
            | (c & 0xFFL) << 8
            | (c & 0xFFL);
    
    Arrays.fill(str, strOffset, strOffset + (c / Long.SIZE), longValue);
  }
  
  public static LongPtr cast(Object voidPointer) {
    if(voidPointer instanceof MallocThunk) {
      return ((MallocThunk) voidPointer).longPtr();
    }
    return (LongPtr) voidPointer;
  }
  
}

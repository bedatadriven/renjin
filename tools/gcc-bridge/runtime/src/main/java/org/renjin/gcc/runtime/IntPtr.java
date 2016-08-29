package org.renjin.gcc.runtime;


import java.util.Arrays;

public class IntPtr implements Ptr {
  
  public static final IntPtr NULL = new IntPtr();
  
  public final int[] array;
  public final int offset;

  private IntPtr() {
    this.array = null;
    this.offset = 0;
  }

  public IntPtr(int[] array, int offset) {
    this.array = array;
    this.offset = offset;
  }

  public IntPtr(int... array) {
    this.array = array;
    this.offset = 0;
  }

  @Override
  public int[] getArray() {
    return array;
  }

  @Override
  public int getOffset() {
    return offset;
  }

  @Override
  public IntPtr realloc(int newSizeInBytes) {
    return new IntPtr(Realloc.realloc(array, offset, newSizeInBytes / 4));
  }

  @Override
  public Ptr pointerPlus(int bytes) {
    return new IntPtr(array, offset + (bytes / 4));
  }

  public int unwrap() {
    return array[offset];
  }
  
  public int get() {
    return array[offset];
  }
  
  public void set(int value) {
    array[offset] = value;
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
   * @param byteValue the byte value to set
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
  
  public static IntPtr cast(Object voidPointer) {
    if(voidPointer instanceof MallocThunk) {
      return ((MallocThunk) voidPointer).intPtr();
    } 
    return (IntPtr) voidPointer;
  }

}

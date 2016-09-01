package org.renjin.gcc.runtime;


import java.util.Arrays;

public class DoublePtr implements Ptr {
  
  public static final DoublePtr NULL = new DoublePtr();
  
  public static final int BYTES = Double.SIZE / 8;
  
  public final double[] array;
  public final int offset;

  private DoublePtr() {
    this.array = null;
    this.offset = 0;
  }

  public DoublePtr(double[] array, int offset) {
    this.array = array;
    this.offset = offset;
  }

  public DoublePtr(double... values) {
    this.array = values;
    this.offset = 0;
  }

  @Override
  public double[] getArray() {
    return array;
  }

  @Override
  public int getOffset() {
    return offset;
  }

  @Override
  public DoublePtr realloc(int newSizeInBytes) {
    return new DoublePtr(Realloc.realloc(array, offset, newSizeInBytes / 8));
  }

  @Override
  public Ptr pointerPlus(int bytes) {
    return new DoublePtr(array, offset + (bytes / 8));
  }

  @Override
  public String toString() {
    return offset + "+" + Arrays.toString(array);
  }

  public double unwrap() {
    return array[offset];
  }

  public double get() {
    return array[offset];
  }
  
  public double get(int i) {
    return array[offset+i];
  }

  public void set(double x) {
    array[offset] = x;
  }
  
  public void set(int index, double value) {
    array[offset+index] = value;
  }

  /**
   * Performs a byte-by-byte comparison of the given double arrays.
   *
   * @param x the first pointer
   * @param y the second pointer
   * @param numBytes the number of <strong>bytes</strong> to compare
   * @return 0 if the two arrrays are byte-for-byte equal, or -1 if the first 
   * array is less than the second array, or > 0 if the second array is greater than the first array
   */
  public static int memcmp(DoublePtr x, DoublePtr y, int numBytes) {
    return memcmp(x.array, x.offset, y.array, y.offset, numBytes);
  }
  
  /**
   * Performs a byte-by-byte comparison of the given double arrays.
   * 
   * @param x the first array
   * @param xi the start index of the first array
   * @param y the second array
   * @param yi the start index of the second array
   * @param n the number of <strong>bytes</strong> to compare
   * @return 0 if the two arrrays are byte-for-byte equal, or -1 if the first 
   * array is less than the second array, or > 0 if the second array is greater than the first array
   */
  public static int memcmp(double[] x, int xi, double[] y, int yi, int n) {
    while(n > 0) {
      long xb = Double.doubleToRawLongBits(xi);
      long yb = Double.doubleToRawLongBits(yi);
      if(xb != yb || n < 8) {
        LongPtr.memcmp(xb, yb, n);
      }
      xi++;
      yi++;
      n -= 8;
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

    assert n % BYTES == 0;

    double doubleValue = memset(c);

    Arrays.fill(str, strOffset, strOffset + (n / BYTES), doubleValue);
  }

  /**
   * Sets all bytes of a {@code double} value to {@code c}
   */
  public static double memset(int c) {
    return Double.longBitsToDouble(LongPtr.memset(c));
  }

  public static DoublePtr cast(Object voidPointer) {
    if(voidPointer instanceof MallocThunk) {
      return ((MallocThunk) voidPointer).doublePtr();
    }
    return (DoublePtr) voidPointer;
  }
}

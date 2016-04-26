package org.renjin.gcc.runtime;


import java.util.Arrays;

public class DoublePtr implements Ptr {
  public double[] array;
  public int offset;

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
  public String toString() {
    return offset + "+" + Arrays.toString(array);
  }

  public double unwrap() {
    return array[offset];
  }

  public double get(int i) {
    return array[offset+i];
  }

  public void set(int index, double value) {
    array[offset+index] = value;
  }
  
  public void update(double[] array,  int offset) {
    this.array = array;
    this.offset = offset;
  }

  /**
   * Performs a byte-by-byte comparison of the given double arrays.
   *
   * @param x the first pointer
   * @param y the second pointer
   * @param n the number of <strong>bytes</strong> to compare
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

    assert n % Double.SIZE == 0;

    long longValue =  
        (c & 0xFFL) << 56
        | (c & 0xFFL) << 48
        | (c & 0xFFL) << 40
        | (c & 0xFFL) << 32
        | (c & 0xFFL) << 24
        | (c & 0xFFL) << 16
        | (c & 0xFFL) << 8
        | (c & 0xFFL);
    
    double doubleValue = Double.longBitsToDouble(longValue);
    
    Arrays.fill(str, strOffset, strOffset + (c / Double.SIZE), doubleValue);
  }


}

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
}

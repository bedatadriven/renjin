/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.gcc.runtime;


import java.util.Arrays;

public class LongPtr extends AbstractPtr {
  
  public static final LongPtr NULL = new LongPtr();
  
  public final long[] array;
  public final int offset;

  private LongPtr() {
    this.array = null;
    this.offset = 0;
  }

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

  @Override
  public byte getByte(int offset) {
    int bytes = (this.offset * 8) + offset;
    int index = bytes / 8;
    double element = array[index];
    long elementBits = Double.doubleToRawLongBits(element);
    int shift = (bytes % 8) * 8;

    return (byte)(elementBits >>> shift);  }

  @Override
  public void setByte(int offset, byte value) {
    throw new UnsupportedOperationException("TODO");
  }

  public long unwrap() {
    return array[offset];
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

    Arrays.fill(str, strOffset, strOffset + (c / Long.SIZE), memset(c));
  }

  /**
   * Sets all bytes of a long value to the byte {@code c}
   */
  public static long memset(int c) {
    return (c & 0xFFL) << 56
        | (c & 0xFFL) << 48
        | (c & 0xFFL) << 40
        | (c & 0xFFL) << 32
        | (c & 0xFFL) << 24
        | (c & 0xFFL) << 16
        | (c & 0xFFL) << 8
        | (c & 0xFFL);
  }

  public static LongPtr cast(Object voidPointer) {
    if(voidPointer instanceof MallocThunk) {
      return ((MallocThunk) voidPointer).longPtr();
    }
    if(voidPointer == null) {
      return NULL;
    }
    return (LongPtr) voidPointer;
  }
  
}

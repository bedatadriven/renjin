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


import java.math.BigInteger;
import java.util.Arrays;

public class LongPtr extends AbstractPtr {

  public static final int BYTES = 8;

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
  public int getOffsetInBytes() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public LongPtr realloc(int newSizeInBytes) {
    return new LongPtr(Realloc.realloc(array, offset, newSizeInBytes / 8));
  }

  @Override
  public Ptr pointerPlus(int bytes) {
    if(bytes == 0) {
      return this;
    }
    if(bytes % BYTES == 0) {
      return new LongPtr(array, offset + (bytes / BYTES));
    } else {
      return new OffsetPtr(this, this.offset * BYTES + bytes);
    }
  }

  @Override
  public byte getByte(int offset) {
    int bytes = (this.offset * BYTES) + offset;
    int index = bytes / BYTES;
    long elementBits = array[index];
    int shift = (bytes % BYTES) * 8;

    return (byte)(elementBits >>> shift);
  }

  @Override
  public void setByte(int offset, byte value) {
    int bytes = (this.offset * BYTES) + offset;
    int index = bytes / BYTES;
    int shift = (bytes % BYTES) * BITS_PER_BYTE;

    long element = array[index];

    long updateMask = 0xFF << shift;

    // Zero out the bits in the byte we are going to update
    element = element & ~updateMask;

    // Shift our byte into position
    long update = (((long)value) << shift) & updateMask;

    // Merge the original long and updated bits together
    array[index] = element | update;
  }

  @Override
  public int toInt() {
    return offset * BYTES;
  }

  @Override
  public boolean isNull() {
    return array == null && offset == 0;
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
  
  public static void memcpy(LongPtr x, LongPtr y, int numBytes) {
    long[] arrayS = y.getArray();
    int offsetS = y.getOffset();
    int restY = arrayS.length - offsetS;
    if(restY > 0) {
      long[] carray = new long[numBytes];
      for(int i = 0, j = offsetS; j < arrayS.length && i < numBytes; j++, i++) {
        carray[i] = arrayS[j];
      }
      x = new LongPtr(carray);
    }
  }

  public static double unsignedInt64ToReal64(long i) {
    if(i >= 0) {
      return (double)i;
    }
    int upper = (int) (i >>> 32);
    int lower = (int) i;

    long lowerLong = ((long) lower) & 0xffffffffL;
    long upperLong = ((long) upper) & 0xffffffffL;

    return ((double)lowerLong) + ((double)upperLong) * 4294967296d;
  }

  /**
   * Returns dividend / divisor, where the dividend and divisor are treated as unsigned 64-bit
   * quantities. (Copied from Guava 17.0)
   *
   * @param dividend the dividend (numerator)
   * @param divisor the divisor (denominator)
   * @throws ArithmeticException if divisor is 0
   */
  public static long unsignedDivide(long dividend, long divisor) {
    if (divisor < 0) { // i.e., divisor >= 2^63:
      if (compareUnsigned(dividend, divisor) < 0) {
        return 0; // dividend < divisor
      } else {
        return 1; // dividend >= divisor
      }
    }

    // Optimization - use signed division if dividend < 2^63
    if (dividend >= 0) {
      return dividend / divisor;
    }

    /*
     * Otherwise, approximate the quotient, check, and correct if necessary. Our approximation is
     * guaranteed to be either exact or one less than the correct value. This follows from fact
     * that floor(floor(x)/i) == floor(x/i) for any real x and integer i != 0. The proof is not
     * quite trivial. (Copied from Guava 17.0)
     */
    long quotient = ((dividend >>> 1) / divisor) << 1;
    long rem = dividend - quotient * divisor;
    return quotient + (compareUnsigned(rem, divisor) >= 0 ? 1 : 0);
  }

  /**
   * Returns the unsigned remainder from dividing the first argument
   * by the second where each argument and the result is interpreted
   * as an unsigned value. (Copied from OpenJDK 1.8)
   *
   * @param dividend the value to be divided
   * @param divisor the value doing the dividing
   * @return the unsigned remainder of the first argument divided by
   * the second argument
   * @see #divideUnsigned
   * @since 1.8
   */
  public static long unsignedRemainder(long dividend, long divisor) {
    if (dividend > 0 && divisor > 0) { // signed comparisons
      return dividend % divisor;
    } else {
      if (compareUnsigned(dividend, divisor) < 0) {
        // Avoid explicit check for 0 divisor
        return dividend;
      } else {
        return toUnsignedBigInteger(dividend).
            remainder(toUnsignedBigInteger(divisor)).longValue();
      }
    }
  }


  /**
   * Return a BigInteger equal to the unsigned value of the
   * argument. (Copied from OpenJDK 1.8)
   */
  private static BigInteger toUnsignedBigInteger(long i) {
    if (i >= 0L) {
      return BigInteger.valueOf(i);
    } else {
      int upper = (int) (i >>> 32);
      int lower = (int) i;

      // return (upper << 32) + lower
      return (BigInteger.valueOf(toUnsignedLong(upper))).shiftLeft(32).
          add(BigInteger.valueOf(toUnsignedLong(lower)));
    }
  }

  private static long toUnsignedLong(int x) {
    return x & 0xFFFFFFFFL;
  }


  /**
   * A (self-inverse) bijection which converts the ordering on unsigned longs to the ordering on
   * longs, that is, {@code a <= b} as unsigned longs if and only if {@code flip(a) <= flip(b)}
   * as signed longs. (Copied from Guava 17.0)
   */
  private static long flip(long a) {
    return a ^ Long.MIN_VALUE;
  }

  /**
   * Compares the two specified {@code long} values, treating them as unsigned values between
   * {@code 0} and {@code 2^64 - 1} inclusive. (Copied from Guava 17.0)
   *
   * @param a the first unsigned {@code long} to compare
   * @param b the second unsigned {@code long} to compare
   * @return a negative value if {@code a} is less than {@code b}; a positive value if {@code a} is
   *         greater than {@code b}; or zero if they are equal
   */
  public static int compareUnsigned(long a, long b) {
    return Long.compare(flip(a), flip(b));
  }

  public static long unsignedMax(long a, long b) {
    if(a == b) {
      return a;
    }
    if(flip(a) > flip(b)) {
      return a;
    } else {
      return b;
    }
  }

  public static long unsignedMin(long a, long b) {
    if(a == b) {
      return a;
    }
    if(flip(a) < flip(b)) {
      return a;
    } else {
      return b;
    }
  }
}

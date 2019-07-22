/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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

public class IntPtr extends AbstractPtr implements Ptr {
  
  public static final int BYTES = Integer.SIZE / 8;

  public static final IntPtr NULL = new IntPtr();
  public static final int UNINITIALIZED_VALUE = 0xCCCCCCCC;

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

  public static IntPtr malloc(int bytes) {
    return new IntPtr(new int[AbstractPtr.mallocSize(bytes, BYTES)]);
  }

  public static Ptr toPtr(int i) {
    if(i == 0) {
      return NULL;
    } else {
      throw new UnsupportedOperationException(String.format("Cannot cast integer %x to pointer", i));
    }
  }

  public static int fromPtr(Ptr ptr) {
    if(ptr.isNull()) {
      return 0;
    } else {
      return ptr.toInt();
    }
  }

  @Override
  public int[] getArray() {
    return array;
  }

  private int getOffset() {
    return offset;
  }

  @Override
  public int getOffsetInBytes() {
    return offset * BYTES;
  }

  @Override
  public IntPtr realloc(int newSizeInBytes) {
    return new IntPtr(Realloc.realloc(array, offset, newSizeInBytes / BYTES));
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

    assert n % BYTES == 0;

    Arrays.fill(str, strOffset, strOffset + (n / BYTES), memset(byteValue));
  }

  /**
   * Sets all bytes of an {@code int} to the {@code byteValue}
   */
  public static int memset(int byteValue) {
    return byteValue << 24 |
          (byteValue & 0xFF) << 16 |
          (byteValue & 0xFF) << 8 |
          (byteValue & 0xFF);
  }

  public static int memcmp(IntPtr x, IntPtr y, int n) {
    return memcmp(x.array, x.offset, y.array, y.offset, n);
  }

  public static int memcmp(int[] x, int xi, int[] y, int yi, int n) {
    while(n > 0) {
      int vx = x[xi];
      int vy = y[yi];

      if(vx != vy || n < BYTES) {
        return memcmp(vx, vy, n);
      }
      xi++;
      yi++;
      n-= BYTES;
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
    if(voidPointer == null) {
      return NULL;
    }
    return (IntPtr) voidPointer;
  }

  @Override
  public byte getByte(int offset) {
    return getByteViaInt(offset);
  }

  @Override
  public int getInt() {
    try {
      return this.array[this.offset];
    } catch (ArrayIndexOutOfBoundsException ignored) {
      return UNINITIALIZED_VALUE;
    }
  }

  @Override
  public int getAlignedInt(int index) {
    try {
      return this.array[this.offset + index];
    } catch (ArrayIndexOutOfBoundsException ignored) {
      return UNINITIALIZED_VALUE;
    }
  }

  @Override
  public int getInt(int offset) {
    if(this.offset % BYTES == 0) {
      try {
        return this.array[this.offset + (offset / BYTES)];
      } catch (ArrayIndexOutOfBoundsException ignored) {
        // Accessing a value beyond an array's bounds will not throw an error in C,
        // so we have to do the same...
        return UNINITIALIZED_VALUE;
      }
    } else {
      return super.getInt(offset);
    }
  }

  @Override
  public void setInt(int value) {
    this.array[offset] = value;
  }

  @Override
  public void setInt(int byteOffset, int intValue) {
    if(byteOffset % BYTES == 0) {
      this.array[this.offset + (byteOffset / BYTES)] = intValue;
    } else {
      super.setInt(byteOffset, intValue);
    }
  }

  @Override
  public void setAlignedInt(int index, int value) {
    this.array[this.offset + index] = value;
  }

  @Override
  public void setByte(int offset, byte value) {
    setByteViaInt(offset, value);
  }

  @Override
  public Ptr getPointer(int offset) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public int toInt() {
    return offset * BYTES;
  }

  @Override
  public boolean isNull() {
    return array == null && offset == 0;
  }

  @Override
  public Ptr pointerPlus(int byteCount) {
    if(byteCount % BYTES == 0) {
      return new IntPtr(this.array, this.offset + (byteCount / BYTES));
    } else {
      return new OffsetPtr(this, byteCount);
    }
  }


  /**
   * Compares the two specified {@code int} values, treating them as unsigned values between
   * {@code 0} and {@code 2^32 - 1} inclusive.
   *
   * @deprecated Compiler will now use Java 1.8 API
   */
  @Deprecated
  public static int unsignedCompare(int a, int b) {
    return Integer.compareUnsigned(a, b);
  }

  public static int unsignedMax(int a, int b) {
    if(Long.compareUnsigned(a, b) > 0) {
      return a;
    } else {
      return b;
    }
  }

  public static int unsignedMin(int a, int b) {
    if(Long.compareUnsigned(a, b) < 0) {
      return a;
    } else {
      return b;
    }
  }


  /**
   * @deprecated Compiler will now use Java 1.8 API
   */
  @Deprecated
  public static int unsignedDivide(int dividend, int divisor) {
    return Integer.divideUnsigned(dividend, divisor);
  }

  /**
   * @deprecated Compiler will now use Java 1.8 API
   */
  @Deprecated
  public static int unsignedRemainder(int dividend, int divisor) {
    return Integer.remainderUnsigned(dividend, divisor);
  }

  public static void memcpy(IntPtr x, IntPtr y, int numBytes) {
    int[] arrayS = y.getArray();
    int offsetS = y.getOffset();
    int restY = arrayS.length - offsetS;
    if(restY > 0) {
      int[] carray = new int[numBytes];
      for(int i = 0, j = offsetS; j < arrayS.length && i < numBytes; j++, i++) {
        carray[i] = arrayS[j];
      }
      x = new IntPtr(carray);
    }
  }
}

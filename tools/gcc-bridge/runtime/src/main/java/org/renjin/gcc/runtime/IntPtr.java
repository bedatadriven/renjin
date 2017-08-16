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

public class IntPtr extends AbstractPtr implements Ptr {
  
  public static final int BYTES = Integer.SIZE / 8;
  
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
    if(voidPointer == null) {
      return NULL;
    }
    return (IntPtr) voidPointer;
  }

  @Override
  public byte getByte(int offset) {
    int byteIndex = this.offset * 4 + offset;
    int index = byteIndex / 4;
    int shift = (byteIndex % 4) * 8;
    return (byte)(this.array[index] >>> shift);
  }

  @Override
  public int getInt() {
    return this.array[this.offset];
  }

  @Override
  public int getInt(int offset) {
    if(this.offset % 4 == 0) {
      return this.array[this.offset + (offset / 4)];
    } else {
      return super.getInt(offset);
    }
  }

  @Override
  public void setInt(int value) {
    this.array[offset] = value;
  }

  @Override
  public void setInt(int byteOffset, int value) {
    if(byteOffset % 4 == 0) {
      this.array[this.offset + (byteOffset % 4)] = value;
    } else {
      super.setInt(byteOffset, value);
    }
  }

  @Override
  public void setByte(int offset, byte value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public Ptr getPointer(int offset) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public Ptr pointerPlus(int byteCount) {
    if(byteCount % 4 == 0) {
      return new IntPtr(this.array, this.offset + (byteCount / 4));
    } else {
      throw new UnsupportedOperationException("TODO");
    }
  }
}

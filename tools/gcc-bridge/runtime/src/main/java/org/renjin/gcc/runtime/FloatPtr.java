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

public class FloatPtr extends AbstractPtr {

  public static final int BYTES = Float.SIZE / BITS_PER_BYTE;

  public static final FloatPtr NULL = new FloatPtr();
  
  public final float[] array;
  public final int offset;

  private FloatPtr() {
    this.array = null;
    this.offset = 0;
  }

  public FloatPtr(float[] array, int offset) {
    this.array = array;
    this.offset = offset;
  }

  public FloatPtr(float... values) {
    this.array = values;
    this.offset = 0;
  }


  public static FloatPtr malloc(int bytes) {
    return new FloatPtr(new float[AbstractPtr.mallocSize(bytes, BYTES)]);
  }

  @Override
  public float[] getArray() {
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
  public Ptr realloc(int newSizeInBytes) {
    return new FloatPtr(Realloc.realloc(array, offset, newSizeInBytes / 4));
  }

  @Override
  public Ptr pointerPlus(int bytes) {
    return new FloatPtr(array, offset + (bytes / 4));
  }

  @Override
  public byte getByte(int offset) {
    int bytes = (this.offset * BYTES) + offset;
    int index = bytes / BYTES;
    float element = array[index];
    long elementBits = Float.floatToRawIntBits(element);
    int shift = (bytes % BYTES) * BITS_PER_BYTE;

    return (byte)(elementBits >>> shift);
  }

  @Override
  public void setByte(int offset, byte value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public int toInt() {
    return offset * 4;
  }

  @Override
  public boolean isNull() {
    return array == null && offset == 0;
  }

  @Override
  public String toString() {
    return offset + "+" + Arrays.toString(array);
  }

  public float unwrap() {
    return array[offset];
  }

  public float get(int i) {
    return array[offset+i];
  }

  public void set(int index, float value) {
    array[offset+index] = value;
  }

  /**
   * Copies the character c (an unsigned char) to 
   * the first n characters of the string pointed to, by the argument str.
   *
   * @param str an array of floats
   * @param strOffset the first element to set
   * @param c the byte value to set
   * @param n the number of bytes to set
   */
  public static void memset(double[] str, int strOffset, int c, int n) {

    assert n % BYTES == 0;

    float floatValue = memset(c);

    Arrays.fill(str, strOffset, strOffset + (n / BYTES), floatValue);
  }

  /**
   * Sets all bytes of a {@code float} value to {@code c}
   */
  public static float memset(int c) {
    return Float.intBitsToFloat(IntPtr.memset(c));
  }
  
  public static FloatPtr cast(Object voidPointer) {
    if(voidPointer instanceof MallocThunk) {
      return ((MallocThunk) voidPointer).floatPtr();
    }
    if(voidPointer == null) {
      return NULL;
    }
    return (FloatPtr) voidPointer;
  }
}

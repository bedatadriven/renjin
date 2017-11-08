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


public class ShortPtr extends AbstractPtr {

  public static final int BYTES = 2;

  public static final ShortPtr NULL = new ShortPtr();
  
  public final short[] array;
  public final int offset;
  
  private ShortPtr() {
    this.array = null;
    this.offset = 0;
  }

  public ShortPtr(short[] array, int offset) {
    this.array = array;
    this.offset = offset;
  }

  public ShortPtr(short... array) {
    this.array = array;
    this.offset = 0;
  }

  public static ShortPtr malloc(int bytes) {
    return new ShortPtr(new short[AbstractPtr.mallocSize(bytes, BYTES)]);
  }

  @Override
  public short[] getArray() {
    return array;
  }

  @Override
  public int getOffset() {
    return offset;
  }

  @Override
  public int getOffsetInBytes() {
    return offset * BYTES;
  }

  @Override
  public Ptr realloc(int newSizeInBytes) {
    return new ShortPtr(Realloc.realloc(array, offset, newSizeInBytes / 2));
  }

  @Override
  public Ptr pointerPlus(int bytes) {
    return new ShortPtr(array, offset + (bytes / 2));
  }

  @Override
  public short getShort() {
    return array[this.offset];
  }

  @Override
  public short getAlignedShort(int index) {
    return array[this.offset + index];
  }

  @Override
  public short getShort(int offset) {
    if(offset % BYTES == 0) {
      return getAlignedShort(offset / BYTES);
    } else {
      return super.getShort(offset);
    }
  }

  @Override
  public void setAlignedShort(int index, short shortValue) {
    array[this.offset + index] = shortValue;
  }

  @Override
  public void setShort(short value) {
    array[this.offset] = value;
  }

  @Override
  public void setShort(int offset, short value) {
    if(offset % BYTES == 0) {
      setAlignedShort(offset / BYTES, value);
    } else {
      super.setShort(offset, value);
    }
  }

  @Override
  public byte getByte(int offset) {
    int byteIndex = this.offset * BYTES + offset;
    int index = byteIndex / BYTES;
    int shift = (byteIndex % BYTES) * 8;
    return (byte)(this.array[index] >>> shift);
  }

  @Override
  public void setByte(int offset, byte value) {
    int bytes = (this.offset * BYTES) + offset;
    int index = bytes / BYTES;
    int shift = (bytes % BYTES) * BITS_PER_BYTE;

    int element = array[index];

    int updateMask = 0xFF << shift;

    // Zero out the bits in the byte we are going to update
    element = element & ~updateMask;

    // Shift our byte into position
    int update = (((int)value) << shift) & updateMask;

    // Merge the original long and updated bits together
    array[index] = (short)(element | update);
  }

  @Override
  public int toInt() {
    return offset * BYTES;
  }

  @Override
  public boolean isNull() {
    return array == null && offset != 0;
  }

  public short unwrap() {
    return array[offset];
  }

  @Override
  public String toString() {
    return offset + "+" + Arrays.toString(array);
  }


  public static void memset(short[] array, int offset, int value, int length) {
    throw new UnsupportedOperationException("TODO");
  }

  public static short memset(int byteValue) {
    throw new UnsupportedOperationException("TODO");
  }

  public static ShortPtr cast(Object voidPointer) {
    if(voidPointer instanceof MallocThunk) {
      return ((MallocThunk) voidPointer).shortPtr();
    }
    if(voidPointer == null) {
      return NULL;
    }
    return (ShortPtr) voidPointer;  
  }
}

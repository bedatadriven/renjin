/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */

package org.renjin.gcc.runtime;

/**
 * Pointer implementation backed by an array of integers, with an
 * arbitrary offset in bytes.
 */
public class IntPtrUnaligned extends AbstractPtr {

  private int array[];

  /**
   * Offset of this pointer, in bytes, to the array.
   */
  private int offset;

  public IntPtrUnaligned(int value) {
    this.array = new int[] { value };
  }

  public IntPtrUnaligned(int[] array, int offset) {
    this.array = array;
    this.offset = offset;
  }

  /**
   * Creates a new array-backed virtual pointer
   * @param array an array of integers
   * @param offset the offset, in <strong>elements</strong>, not bytes.
   */
  public static IntPtrUnaligned fromPair(int[] array, int offset) {
    return new IntPtrUnaligned(array, offset * 4);
  }


  @Override
  public byte getByte(int offset) {
    int byteOffset = this.offset + offset;
    int element = byteOffset / 4;
    int shift = (byteOffset % 4) * 8;
    return (byte)(this.array[element] >> shift);
  }

  @Override
  public void setByte(int offset, byte value) {
    throw new UnsupportedOperationException("TODO");
  }


  @Override
  public int getInt(int offset) {
    int byteOffset = this.offset + offset;
    int shift = byteOffset % 4;
    if(shift == 0) {
      return this.array[byteOffset / 4];
    } else {
      return super.getInt(offset);
    }
  }

  @Override
  public void setInt(int offset, int value) {
    int byteOffset = this.offset + offset;
    int shift = byteOffset % 4;
    if(shift == 0) {
      this.array[byteOffset / 4] = value;
    } else {
      super.setInt(offset, value);
    }
  }

  @Override
  public Ptr getPointer() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public Ptr getPointer(int offset) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void setPointer(int offset, Ptr value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public int toInt() {
    return offset;
  }

  @Override
  public Ptr realloc(int newSizeInBytes) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public Ptr pointerPlus(int byteCount) {
    return new IntPtrUnaligned(this.array, offset + byteCount);
  }


}

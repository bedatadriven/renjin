/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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

/**
 * A pointer to one or more pointers.
 */
public class PointerPtr extends AbstractPtr {

  private static final int BYTES = 4;

  public static final PointerPtr NULL = new PointerPtr(null, 0);

  /**
   * An array of pointers. We consider pointers to be 32-bits.
   */
  private Ptr[] array;

  /**
   * Offset from the beginning of the pointer in bytes.
   */
  private int offset;

  public PointerPtr(Ptr[] array) {
    this.array = array;
  }

  /**
   *
   * @param array the storage array
   * @param offset the offset in bytes from the start of the array.
   */
  public PointerPtr(Ptr[] array, int offset) {
    checkAligned(offset);
    this.array = array;
    this.offset = offset;
  }

  public static PointerPtr wrap(Ptr[] array, int offset) {
    if(array == null) {
      return NULL;
    } else {
      return new PointerPtr(array, offset);
    }
  }

  /**
   * Allocates a "memory block" of length 1 and initializes it to the given
   * value.
   *
   * @return a pointer to the new memory block
   */
  public static PointerPtr malloc(Ptr value) {
    return new PointerPtr(new Ptr[] { value }, 0);
  }

  public static PointerPtr malloc(int bytes) {
    Ptr[] array = new Ptr[mallocSize(bytes, BYTES)];
    Arrays.fill(array, NULL);

    return new PointerPtr(array, 0);
  }

  @Override
  public Object getArray() {
    return array;
  }

  @Override
  public int getOffsetInBytes() {
    return offset * BYTES;
  }

  @Override
  public Ptr realloc(int newSizeInBytes) {
    if(this.array == null) {
      return malloc(newSizeInBytes);
    }

    Ptr[] newArray = new Ptr[mallocSize(newSizeInBytes, BYTES)];
    System.arraycopy(this.array, this.offset, newArray, 0, Math.min(this.array.length - this.offset, newArray.length));

    return new PointerPtr(newArray);
  }

  @Override
  public Ptr pointerPlus(int bytes) {
    return new PointerPtr(array, this.offset + bytes);
  }

  @Override
  public byte getByte(int offset) {
    int byteOffset = this.offset * BYTES + offset;
    int index = byteOffset / BYTES;
    Ptr ptr = array[index];

    if(ptr.isNull()) {
      return 0;
    }
    int intValue = ptr.toInt();
    int shift = (byteOffset % BYTES) * 8;
    return (byte)(intValue >>> shift);
  }

  @Override
  public void setAlignedInt(int index, int value) {
    if(value == 0) {
      array[index] = NULL;
    } else {
      super.setAlignedInt(index, value);
    }
  }

  @Override
  public void setInt(int offset, int intValue) {
    if(offset % BYTES == 0 && intValue == 0) {
      array[offset / BYTES] = NULL;
    } else {
      super.setInt(offset, intValue);
    }
  }

  @Override
  public void setByte(int offset, byte value) {
    throw new UnsupportedOperationException("Unsupported primitive store to a memory region allocated for pointers.\n" +
        "This means something went wrong during compilation and we allocated the wrong type of storage.");
  }

  @Override
  public Ptr getPointer(int offset) {
    return (Ptr) array[ checkAligned(this.offset + offset) ];
  }

  @Override
  public void setPointer(int offset, Ptr value) {
    array[ checkAligned(this.offset + offset) ] = value;
  }

  private static int checkAligned(int bytes) {
    if(bytes % BYTES != 0) {
      throw new UnsupportedOperationException("Unaligned access");
    }
    return bytes / BYTES;
  }

  @Override
  public int toInt() {
    return offset;
  }

  @Override
  public boolean isNull() {
    return array == null && offset == 0;
  }

  @Override
  public void memset(int intValue, int n) {
    if( intValue == 0 &&
        offset % BYTES == 0 &&
        n % BYTES == 0)
    {
      // Handle zeroing out specially
      int index = offset / BYTES;
      while(n >= BYTES) {
        array[index] = NULL;
        n -= BYTES;
        index++;
      }
    } else {
      super.memset(intValue, n);
    }
  }

  @Override
  public void memcpy(Ptr source, int numBytes) {
    for (int i = 0; i < numBytes; i+= BYTES) {
      setPointer(i, source.getPointer(i));
    }
    if(numBytes % BYTES != 0) {
      throw new UnsupportedOperationException("TODO");
    }
  }

  @Override
  public Ptr copyOf(int offset, int numBytes) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public Ptr copyOf(int numBytes) {
    throw new UnsupportedOperationException("TODO");
  }
}

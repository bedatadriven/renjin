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

/**
 * Pointer type that references both primitives and garbage-collected
 * references.
 */
public class MixedPtr extends AbstractPtr {

  private static final int POINTER_BYTES = 4;

  private byte[] primitives;
  private Object[] references;

  /**
   * The offset from the beginning of the arrays in
   * bytes.
   */
  private int offset = 0;

  private MixedPtr() {
  }

  private MixedPtr(byte[] primitives, Object[] references, int offset) {
    this.primitives = primitives;
    this.references = references;
    this.offset = offset;
  }

  public static MixedPtr malloc(int bytes) {
    MixedPtr ptr = new MixedPtr();
    ptr.primitives = new byte[bytes];
    ptr.references = new Object[mallocSize(bytes, POINTER_BYTES)];
    return ptr;
  }

  @Override
  public Object getArray() {
    return primitives;
  }

  @Override
  public int getOffsetInBytes() {
    return this.offset;
  }

  @Override
  public Ptr realloc(int newSizeInBytes) {
    if(newSizeInBytes == primitives.length) {
      return this;
    }
    MixedPtr ptr = new MixedPtr();
    ptr.primitives = Arrays.copyOf(this.primitives, newSizeInBytes);
    ptr.references = Arrays.copyOf(this.references, mallocSize(newSizeInBytes, POINTER_BYTES));

    return ptr;
  }

  @Override
  public Ptr pointerPlus(int bytes) {
    if(bytes == 0) {
      return this;
    }
    return new MixedPtr(primitives, references, this.offset + bytes);
  }

  @Override
  public byte getByte(int offset) {
    return primitives[this.offset + offset];
  }

  @Override
  public void setByte(int offset, byte value) {
    primitives[this.offset + offset] = value;
  }

  @Override
  public Ptr getPointer(int offset) {
    int byteStart = this.offset + offset;
    if(byteStart % POINTER_BYTES == 0) {
      int index = byteStart / POINTER_BYTES;

      Ptr ref = (Ptr) references[index];
      if(ref != null) {
        return ref;
      }
    }

    return BytePtr.NULL.pointerPlus(getInt(offset));
  }

  @Override
  public final void setPointer(int offset, Ptr value) {
    int byteStart = this.offset + offset;
    if(byteStart % POINTER_BYTES != 0) {
      throw new UnsupportedOperationException("Unaligned pointer storage");
    }
    int index = byteStart / POINTER_BYTES;
    references[index] = value;

    setInt(offset, value.toInt());
  }


  @Override
  public int toInt() {
    return getOffsetInBytes();
  }

  @Override
  public boolean isNull() {
    return false;
  }

  @Override
  public void memcpy(Ptr source, int numBytes) {
    if(source instanceof MixedPtr && numBytes % POINTER_BYTES == 0) {
      MixedPtr ptr = (MixedPtr) source;
      System.arraycopy(
          ptr.primitives, ptr.offset,
          this.primitives, this.offset,
          numBytes);

      System.arraycopy(
          ptr.references, ptr.offset / POINTER_BYTES,
          this.references, this.offset / POINTER_BYTES,
          numBytes / POINTER_BYTES);

    } else {
      for (int i = 0; i < numBytes; i++) {
        setByte(i, source.getByte(i));
      }
    }
  }

  @Override
  public Ptr copyOf(int offset, int numBytes) {
    MixedPtr copy = new MixedPtr();
    copy.primitives = Arrays.copyOfRange(
        primitives,
        this.offset + offset,
        this.offset + offset + numBytes);
    copy.references = Arrays.copyOfRange(
        references,
        (this.offset + offset) / POINTER_BYTES,
        (this.offset + offset + numBytes) / POINTER_BYTES);

    return copy;
  }
}

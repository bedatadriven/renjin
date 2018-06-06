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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Pointer type that references both primitives and garbage-collected
 * references.
 */
public class MixedPtr extends AbstractPtr {

  private static final int POINTER_BYTES = 4;

  private ByteBuffer primitives;
  private Object[] references;

  /**
   * The offset from the beginning of the arrays in
   * bytes.
   */
  private int offset = 0;

  private MixedPtr() {
  }

  private MixedPtr(ByteBuffer primitives, Object[] references, int offset) {
    this.primitives = primitives;
    this.references = references;
    this.offset = offset;
  }

  public static MixedPtr malloc(int bytes) {
    MixedPtr ptr = new MixedPtr();
    try {
      ptr.primitives = ByteBuffer.allocateDirect(bytes).order(ByteOrder.nativeOrder());
      ptr.references = new Object[mallocSize(bytes, POINTER_BYTES)];
    } catch (OutOfMemoryError e) {
      System.err.println("MixedPtr out of memory");
      throw e;
    }
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
    if(newSizeInBytes == this.primitives.capacity()) {
      return this;
    }

    ByteBuffer source = this.primitives.asReadOnlyBuffer();
    source.position(0);
    source.limit(Math.min(source.capacity(), newSizeInBytes));

    ByteBuffer target = ByteBuffer.allocateDirect(newSizeInBytes).order(ByteOrder.nativeOrder());
    target.put(source);
    target.position(0);
    target.limit(newSizeInBytes);

    MixedPtr ptr = new MixedPtr();
    ptr.primitives = target;
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
    return primitives.get(this.offset + offset);
  }

  @Override
  public void setByte(int offset, byte value) {
    primitives.put(this.offset + offset, value);
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
  public double getDouble() {
    return this.primitives.getDouble(this.offset);
  }

  @Override
  public double getDouble(int offset) {
    return this.primitives.getDouble(this.offset + offset);
  }

  @Override
  public double getAlignedDouble(int index) {
    return this.primitives.getDouble(this.offset + index * DoublePtr.BYTES);
  }

  @Override
  public void setAlignedDouble(int index, double value) {
    this.primitives.putDouble(this.offset + index * DoublePtr.BYTES, value);
  }

  @Override
  public void setDouble(double value) {
    this.primitives.putDouble(this.offset, value);
  }

  @Override
  public void setDouble(int offset, double doubleValue) {
    this.primitives.putDouble(this.offset + offset, doubleValue);
  }

  @Override
  public int getInt() {
    return this.primitives.getInt(this.offset);
  }

  @Override
  public int getAlignedInt(int index) {
    return this.primitives.getInt(this.offset + index * IntPtr.BYTES);
  }

  @Override
  public int getInt(int offset) {
    return this.primitives.getInt(this.offset + offset);
  }

  @Override
  public void setInt(int value) {
    this.primitives.putInt(this.offset, value);
  }

  @Override
  public void setInt(int offset, int intValue) {
    this.primitives.putInt(this.offset + offset, intValue);
  }

  @Override
  public void setAlignedInt(int index, int value) {
    this.primitives.putInt(this.offset + index * IntPtr.BYTES, value);
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
      for (int i = 0; i < numBytes; i++) {
        setByte(i, source.getByte(i));
      }

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

    ByteBuffer source = this.primitives.asReadOnlyBuffer();
    source.position(this.offset + offset);
    source.limit(this.offset + offset + numBytes);

    MixedPtr copy = new MixedPtr();
    copy.primitives = ByteBuffer.allocateDirect(numBytes).order(ByteOrder.nativeOrder());
    copy.primitives.put(source);
    copy.primitives.position(0);
    copy.references = Arrays.copyOfRange(
        references,
        (this.offset + offset) / POINTER_BYTES,
        (this.offset + offset + numBytes) / POINTER_BYTES);

    assert copy.primitives.remaining() == numBytes;

    return copy;
  }
}

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

import java.lang.invoke.MethodHandle;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.renjin.gcc.runtime.AbstractPtr.mallocSize;

/**
 * Pointer type that references both primitives and garbage-collected
 * references.
 */
public class MixedPtr implements Ptr {

  private static final int POINTER_BYTES = 4;
  private static final int UNSIGNED_MASK = 0xFF;

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
    ptr.primitives = ByteBuffer.allocateDirect(bytes);
    ptr.references = new Object[mallocSize(bytes, POINTER_BYTES)];
    return ptr;
  }

  @Override
  public Object getArray() {
    return primitives;
  }

  @Override
  public int getOffset() {
    return offset;
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

    ByteBuffer clone = ByteBuffer.allocateDirect(newSizeInBytes);
    ByteBuffer readOnlyCopy = this.primitives.asReadOnlyBuffer();
    readOnlyCopy.flip();

    MixedPtr ptr = new MixedPtr();
    ptr.primitives = clone.put(readOnlyCopy);
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
  public boolean getBoolean() {
    return getByte(this.offset) != 0;
  }

  @Override
  public boolean getBoolean(int offset) {
    return getByte(this.offset + offset) != 0;
  }

  @Override
  public void setBoolean(int offset, boolean value) {
    setByte(this.offset + offset, (value ? (byte)1 : (byte)0));
  }

  @Override
  public void setBoolean(boolean value) {
    setByte(this.offset, (value ? (byte)1 : (byte)0));
  }

  @Override
  public byte getByte() {
    return this.primitives.get(this.offset);
  }

  @Override
  public byte getByte(int offset) {
    return primitives.get(this.offset + offset);
  }

  @Override
  public void setByte(byte value) {
    this.primitives.put(this.offset, value);
  }

  @Override
  public void setByte(int offset, byte value) {
    primitives.put(this.offset + offset, value);
  }

  @Override
  public short getShort() {
    return this.primitives.getShort(this.offset);
  }

  @Override
  public short getShort(int offset) {
    return this.primitives.getShort(this.offset + offset);
  }

  @Override
  public short getAlignedShort(int index) {
    return this.primitives.getShort(this.offset + index * ShortPtr.BYTES);
  }

  @Override
  public void setShort(short value) {
    this.primitives.putShort(this.offset, value);
  }

  @Override
  public void setAlignedShort(int index, short shortValue) {
    this.primitives.putShort(this.offset + index * ShortPtr.BYTES, shortValue);
  }

  @Override
  public void setShort(int offset, short value) {
    this.primitives.putShort(this.offset + offset, value);
  }

  @Override
  public char getChar() {
    return this.primitives.getChar(this.offset);
  }

  @Override
  public char getAlignedChar(int index) {
    return this.primitives.getChar(this.offset + index * CharPtr.BYTES);
  }

  @Override
  public char getChar(int offset) {
    return this.primitives.getChar(this.offset + offset);
  }

  @Override
  public void setChar(char value) {
    this.primitives.putChar(this.offset, value);
  }

  @Override
  public void setAlignedChar(int index, char value) {
    this.primitives.putChar(this.offset + index * CharPtr.BYTES, value);
  }

  @Override
  public void setChar(int offset, char value) {
    this.primitives.putChar(this.offset + offset, value);
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
    return getDouble(this.offset + index * DoublePtr.BYTES);
  }

  @Override
  public void setDouble(double value) {
    this.primitives.putDouble(this.offset, value);
  }

  @Override
  public void setDouble(int offset, double value) {
    this.primitives.putDouble(offset, value);
  }

  @Override
  public void setAlignedDouble(int index, double value) {
    this.primitives.putDouble(this.offset + index * DoublePtr.BYTES, value);
  }

  @Override
  public double getReal96() {
    return getReal96(this.offset);
  }

  @Override
  public double getReal96(int offset) {
    return Double.longBitsToDouble(getLong(this.offset + offset));
  }

  @Override
  public double getAlignedReal96(int index) {
    return getReal96(this.offset + index * 12);
  }

  @Override
  public void setReal96(double value) {
    setReal96(this.offset, value);
  }

  @Override
  public void setReal96(int offset, double value) {
    setLong(this.offset + offset, Double.doubleToRawLongBits(value));
  }

  @Override
  public void setAlignedReal96(int index, double value) {
    setReal96(this.offset + index * 12, value);
  }

  @Override
  public float getFloat() {
    return this.primitives.getFloat(this.offset);
  }

  @Override
  public float getFloat(int offset) {
    return this.primitives.getFloat(this.offset + offset);
  }

  @Override
  public float getAlignedFloat(int index) {
    return getFloat(this.offset + index * FloatPtr.BYTES);
  }

  @Override
  public void setFloat(float value) {
    this.primitives.putFloat(this.offset, value);
  }

  @Override
  public void setAlignedFloat(int index, float value) {
    setFloat(this.offset + index * FloatPtr.BYTES, value);
  }

  @Override
  public void setFloat(int offset, float value) {
    this.primitives.putFloat(this.offset + offset, value);
  }

  @Override
  public int getInt() {
    return this.primitives.getInt(this.offset);
  }

  @Override
  public int getInt(int offset) {
    return this.primitives.getInt(this.offset + offset);
  }

  @Override
  public int getAlignedInt(int index) {
    return getInt(this.offset + index * IntPtr.BYTES);
  }

  @Override
  public void setInt(int value) {
    this.primitives.putInt(this.offset, value);
  }

  @Override
  public void setInt(int offset, int value) {
    this.primitives.putInt(this.offset + offset, value);
  }

  @Override
  public void setAlignedInt(int index, int value) {
    this.primitives.putInt(this.offset + index * IntPtr.BYTES, value);
  }

  @Override
  public long getLong() {
    return this.primitives.getLong(this.offset);
  }

  @Override
  public long getLong(int offset) {
    return this.primitives.getLong(this.offset + offset);
  }

  @Override
  public long getAlignedLong(int index) {
    return getLong(this.offset + index * LongPtr.BYTES);
  }

  @Override
  public void setLong(long value) {
    this.primitives.putLong(this.offset, value);
  }

  @Override
  public void setLong(int offset, long value) {
    this.primitives.putLong(this.offset + offset, value);
  }

  @Override
  public void setAlignedLong(int index, long value) {
    setLong(this.offset + index * LongPtr.BYTES, value);
  }

  @Override
  public Ptr getPointer() {
    if(this.offset % POINTER_BYTES == 0) {
      int index = this.offset / POINTER_BYTES;

      Ptr ref = (Ptr) this.references[index];
      if (ref != null) {
        return ref;
      }
    }

    return BytePtr.NULL.pointerPlus(getInt());
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
  public Ptr getAlignedPointer(int index) {
    return getPointer(index * 4);
  }

  @Override
  public void setPointer(Ptr value) {
    if(this.offset % POINTER_BYTES != 0) {
      throw new UnsupportedOperationException("Unaligned pointer storage");
    }
    int index = this.offset / POINTER_BYTES;
    this.references[index] = value;

    this.primitives.putInt(this.offset, value.toInt());
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
  public void setAlignedPointer(int index, Ptr value) {
    setPointer(this.offset + index * 4, value);
  }

  @Override
  public int toInt() {
    return offset;
  }

  @Override
  public void memset(int intValue, int n) {
    byte byteValue = (byte)intValue;
    for (int i = 0; i < n; i++) {
      setByte(i, byteValue);
    }
  }

  @Override
  public boolean isNull() {
    return primitives == null;
  }

  @Override
  public MethodHandle toMethodHandle() {
    if(isNull()) {
      return null;
    } else {
      return FunctionPtr.getBadHandle();
    }
  }

  @Override
  public int compareTo(Ptr o) {
    return compare(this, o);
  }

  public static int compare(Ptr x, Ptr y) {
    Object m1 = x.getArray();
    Object m2 = y.getArray();

    if(m1 != m2) {
      return Integer.compare(System.identityHashCode(m1), System.identityHashCode(m2));
    }

    if(x.isNull() && y.isNull()) {
      return 0;
    }

    return Integer.compare(x.getOffsetInBytes(), y.getOffsetInBytes());
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
  public void memmove(Ptr source, int numBytes) {
    this.primitives.put(this.offset, source.getByte(numBytes));
  }

  @Override
  public int memcmp(Ptr that, int numBytes) {
    for (int i = 0; i < numBytes; i++) {
      int b1 = this.getByte(i) & UNSIGNED_MASK;
      int b2 = that.getByte(i) & UNSIGNED_MASK;
      if(b1 < b2) {
        return -1;
      } else if(b1 > b2) {
        return 1;
      }
    }
    return 0;
  }

  @Override
  public Ptr copyOf(int offset, int numBytes) {
    ByteBuffer readOnlyCopy = this.primitives.asReadOnlyBuffer();
    readOnlyCopy.flip();

    ByteBuffer clone = ByteBuffer.allocateDirect(numBytes);
    MixedPtr ptr = new MixedPtr();
    for (int i = 0; i < numBytes; i++) {
      clone.put(i , readOnlyCopy.get(i));
    }
    ptr.primitives = clone;

    ptr.references = Arrays.copyOfRange(
        references,
        (this.offset + offset) / POINTER_BYTES,
        (this.offset + offset + numBytes) / POINTER_BYTES);

    return ptr;
  }

  @Override
  public Ptr copyOf(int numBytes) {
    ByteBuffer clone = ByteBuffer.allocateDirect(numBytes);
    ByteBuffer readOnlyCopy = this.primitives.asReadOnlyBuffer();
    readOnlyCopy.flip();

    MixedPtr ptr = new MixedPtr();
    for (int i = 0; i < numBytes; i++) {
      clone.put(i , readOnlyCopy.get(i));
    }

    ptr.primitives = clone;
    ptr.references = Arrays.copyOfRange(
        references,
        this.offset / POINTER_BYTES,
        (this.offset + numBytes) / POINTER_BYTES);

    return ptr;
  }

  @Override
  public Ptr withOffset(int offset) {
    return pointerPlus(offset - getOffsetInBytes());
  }
}

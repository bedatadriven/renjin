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


import java.lang.invoke.MethodHandle;

public abstract class AbstractPtr implements Ptr {

  public static final int BITS_PER_BYTE = 8;

  private static final int UNSIGNED_MASK = 0xFF;

  /**
   * Computes the number of elements to "malloc" given the bytes requested and the size
   * of the elements in bytes.
   *
   * <p>We need to be sure that we allocate enough space if an odd number of bytes is requested. For example,
   * if we allocate an array of ints with a size of 9 bytes, we have to allocate 3 ints, not 2.</p>
   *
   * @param bytes the number of bytes requested
   * @param size the size of the elements, in bytes
   * @return the number of elements to allocate
   */
  static int mallocSize(int bytes, int size) {
    int count = bytes / size;
    if(bytes % size != 0) {
      count++;
    }
    return count;
  }

  @Deprecated
  @Override
  public int getOffset() {
    throw new UnsupportedOperationException("No longer supported. Please recompile.");
  }

  @Override
  public void setShort(short value) {
    setShort(0, value);
  }

  @Override
  public void setShort(int offset, short value) {

    int intValue = value;

    setByte(offset, (byte)(intValue & 0xff));
    intValue >>= BITS_PER_BYTE;

    setByte(offset + 1, (byte)(intValue & 0xff));
  }

  @Override
  public short getShort() {
    return getShort(0);
  }

  @Override
  public short getShort(int offset) {
    return (short)(
        ((getByte(offset + 1) & 0xff) <<  8L) |
        ((getByte(offset    ) & 0xff)));
  }

  @Override
  public short getAlignedShort(int index) {
    return getShort(index * ShortPtr.BYTES);
  }

  @Override
  public void setAlignedShort(int index, short shortValue) {
    setShort(index * ShortPtr.BYTES, shortValue);
  }

  @Override
  public double getDouble() {
    return getDouble(0);
  }

  @Override
  public double getDouble(int offset) {
    return Double.longBitsToDouble(getLong(offset));
  }

  @Override
  public double getAlignedDouble(int index) {
    return getDouble(index * 8);
  }

  @Override
  public void setAlignedDouble(int index, double value) {
    setDouble(index * DoublePtr.BYTES, value);
  }

  @Override
  public char getChar() {
    return getChar(0);
  }

  @Override
  public void setAlignedChar(int index, char value) {
    setChar(index * CharPtr.BYTES, value);
  }

  @Override
  public boolean getBoolean() {
    return getByte() != 0;
  }

  @Override
  public boolean getBoolean(int offset) {
    return getByte(offset) != 0;
  }

  @Override
  public void setBoolean(int offset, boolean value) {
    setByte(offset, (value ? (byte)1 : (byte)0));
  }

  @Override
  public void setBoolean(boolean value) {
    setByte((value ? (byte)1 : (byte)0));
  }

  @Override
  public char getChar(int offset) {
    //return (char) ((b1 << 8) | (b2 & 0xFF));
    byte b1 = getByte(offset + 1);
    byte b2 = getByte(offset + 0);

    return (char) ((b1 << 8) | (b2 & 0xFF));
  }

  @Override
  public int getInt() {
    return getAlignedInt(0);
  }

  @Override
  public int getAlignedInt(int index) {
    return getInt(index * IntPtr.BYTES);
  }

  @Override
  public int getInt(int offset) {
    return
        ((getByte(offset + 3) & 0xff) << 24L) |
        ((getByte(offset + 2) & 0xff) << 16L) |
        ((getByte(offset + 1) & 0xff) <<  8L) |
        ((getByte(offset    ) & 0xff)       );
  }

  @Override
  public byte getByte() {
    return getByte(0);
  }

  @Override
  public long getLong() {
    return getLong(0);
  }

  @Override
  public long getLong(int offset) {
    return ((getByte(offset + 7) & 0xffL) << 56L) |
           ((getByte(offset + 6) & 0xffL) << 48L) |
           ((getByte(offset + 5) & 0xffL) << 40L) |
           ((getByte(offset + 4) & 0xffL) << 32L) |
           ((getByte(offset + 3) & 0xffL) << 24L) |
           ((getByte(offset + 2) & 0xffL) << 16L) |
           ((getByte(offset + 1) & 0xffL) <<  8L) |
           ((getByte(offset    ) & 0xffL)       );
  }

  @Override
  public float getFloat() {
    return getFloat(0);
  }

  @Override
  public float getFloat(int offset) {
    return Float.intBitsToFloat(getInt(offset));
  }

  @Override
  public float getAlignedFloat(int index) {
    return getFloat(index * FloatPtr.BYTES);
  }

  @Override
  public Ptr getPointer() {
    return getPointer(0);
  }

  @Override
  public void setFloat(float value) {
    setFloat(0, value);
  }

  @Override
  public void setInt(int value) {
    setInt(0, value);
  }

  @Override
  public void setChar(char value) {
    setChar(0, value);
  }

  @Override
  public void setByte(byte value) {
    setByte(0, value);
  }

  @Override
  public void setDouble(double value) {
    setDouble(0, value);
  }

  @Override
  public void setLong(long value) {
    setLong(0, value);
  }

  @Override
  public void setPointer(Ptr value) {
    setPointer(0, value);
  }

  @Override
  public void setChar(int offset, char value) {

    int intValue = value;

    setByte(offset, (byte)(intValue & 0xff));
    intValue >>= BITS_PER_BYTE;

    setByte(offset + 1, (byte)(intValue & 0xff));
  }

  @Override
  public void setDouble(int offset, double doubleValue) {
    setLong(offset, Double.doubleToRawLongBits(doubleValue));
  }

  @Override
  public void setFloat(int offset, float value) {
    setInt(offset, Float.floatToRawIntBits(value));
  }

  @Override
  public void setInt(int offset, int intValue) {
    for (int i = 0; i < IntPtr.BYTES; i++) {
      setByte(offset + i, (byte)(intValue & 0xff));
      intValue >>= BITS_PER_BYTE;
    }
  }

  @Override
  public void setLong(int offset, long longValue) {
    for (int i = 0; i < LongPtr.BYTES; i++) {
      setByte(offset + i, (byte)(longValue & 0xffL));
      longValue >>= BITS_PER_BYTE;
    }
  }

  @Override
  public char getAlignedChar(int index) {
    return getChar(index * CharPtr.BYTES);
  }

  @Override
  public void setAlignedFloat(int index, float value) {
    setFloat(index * FloatPtr.BYTES, value);
  }

  @Override
  public void setAlignedInt(int index, int value) {
    setInt(index * IntPtr.BYTES, value);
  }

  @Override
  public void setAlignedLong(int index, long value) {
    setLong(index * LongPtr.BYTES, value);
  }

  @Override
  public Ptr getAlignedPointer(int index) {
    return getPointer(index * 4);
  }

  @Override
  public void setAlignedPointer(int index, Ptr value) {
    setPointer(index * 4, value);
  }

  @Override
  public Ptr getPointer(int offset) {
    return BytePtr.NULL.pointerPlus(getInt(offset));
  }

  @Override
  public void setPointer(int offset, Ptr value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void memset(int intValue, int n) {
    byte byteValue = (byte)intValue;
    for (int i = 0; i < n; i++) {
      setByte(i, byteValue);
    }
  }

  @Override
  public void memcpy(Ptr source, int numBytes) {
    for (int i = 0; i < numBytes; i++) {
      setByte(i, source.getByte(i));
    }
  }

  @Override
  public void memmove(Ptr source, int numBytes) {
    byte buffer[] = new byte[numBytes];
    for (int i = 0; i < numBytes; i++) {
      buffer[i] = source.getByte(i);
    }
    for (int i = 0; i < numBytes; i++) {
      setByte(i, buffer[i]);
    }
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
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public Ptr copyOf(int numBytes) {
    return copyOf(0, numBytes);
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
  public final int compareTo(Ptr o) {

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
  public final boolean equals(Object obj) {

    if(!(obj instanceof Ptr)) {
      return false;
    }

    Ptr that = ((Ptr) obj);

    return this.getArray() == that.getArray() &&
           this.getOffsetInBytes() == that.getOffsetInBytes();
  }
}


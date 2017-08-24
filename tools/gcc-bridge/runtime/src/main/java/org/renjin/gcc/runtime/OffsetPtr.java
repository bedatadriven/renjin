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
 * Created by alex on 24-8-17.
 */
public class OffsetPtr implements Ptr {
  private Ptr ptr;
  private int offset;

  public OffsetPtr(Ptr ptr, int offset) {
    this.ptr = ptr;
    this.offset = offset;
  }

  @Override
  public Object getArray() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public int getOffset() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public Ptr realloc(int newSizeInBytes) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public Ptr pointerPlus(int bytes) {
    int newOffset = this.offset + bytes;
    if(newOffset == 0) {
      return ptr;
    } else {
      return new OffsetPtr(ptr, newOffset);
    }
  }

  @Override
  public byte getByte() {
    return ptr.getByte(offset);
  }

  @Override
  public byte getByte(int offset) {
    return ptr.getByte(this.offset + offset);
  }

  @Override
  public void setByte(byte value) {
    ptr.setByte(this.offset, value);
  }

  @Override
  public void setByte(int offset, byte value) {
    ptr.setByte(this.offset + offset, value);
  }

  @Override
  public short getShort() {
    return ptr.getShort(this.offset);
  }

  @Override
  public short getShort(int offset) {
    return ptr.getShort(this.offset + offset);
  }

  @Override
  public void setShort(short value) {
    ptr.setShort(this.offset, value);
  }

  @Override
  public void setShort(int offset, short value) {
    ptr.setShort(this.offset + offset, value);
  }

  @Override
  public char getChar() {
    return ptr.getChar(this.offset);
  }

  @Override
  public char getChar(int offset) {
    return ptr.getChar(this.offset + offset);
  }

  @Override
  public void setChar(char value) {
    ptr.setChar(this.offset, value);
  }

  @Override
  public void setChar(int offset, char value) {
    ptr.setChar(this.offset + offset, value);
  }

  @Override
  public double getDouble() {
    return ptr.getDouble(this.offset);
  }

  @Override
  public double getDouble(int offset) {
    return ptr.getDouble(this.offset + offset);
  }

  @Override
  public double getAlignedDouble(int index) {
    return ptr.getDouble(this.offset + (index * DoublePtr.BYTES));
  }

  @Override
  public void setDouble(double value) {
    ptr.setDouble(this.offset, value);
  }

  @Override
  public void setDouble(int offset, double value) {
    ptr.setDouble(this.offset + offset, value);
  }

  @Override
  public float getFloat() {
    return ptr.getFloat(this.offset);
  }

  @Override
  public float getFloat(int offset) {
    return ptr.getFloat(this.offset + offset);
  }

  @Override
  public void setFloat(float value) {
    ptr.setFloat(this.offset, value);
  }

  @Override
  public void setFloat(int offset, float value) {
    ptr.setFloat(this.offset + offset, value);
  }

  @Override
  public int getInt() {
    return ptr.getInt(this.offset);
  }

  @Override
  public int getInt(int offset) {
    return ptr.getInt(this.offset + offset);
  }

  @Override
  public int getIntAligned(int index) {
    return ptr.getInt(this.offset + (index * IntPtr.BYTES));
  }

  @Override
  public void setInt(int value) {
    ptr.setInt(this.offset, value);
  }

  @Override
  public void setInt(int offset, int value) {
    ptr.setInt(this.offset + offset, value);
  }

  @Override
  public long getLong() {
    return ptr.getLong(this.offset);
  }

  @Override
  public long getLong(int offset) {
    return ptr.getLong(this.offset + offset);
  }

  @Override
  public void setLong(long value) {
    ptr.setLong(this.offset, value);
  }

  @Override
  public void setLong(int offset, long value) {
    ptr.setLong(this.offset + offset, value);
  }

  @Override
  public Ptr getPointer() {
    return ptr.getPointer(this.offset);
  }

  @Override
  public Ptr getPointer(int offset) {
    return ptr.getPointer(this.offset + offset);
  }

  @Override
  public void setPointer(Ptr value) {
    ptr.setPointer(this.offset, value);
  }

  @Override
  public void setPointer(int offset, Ptr value) {
    ptr.setPointer(this.offset + offset, value);
  }

  @Override
  public int toInt() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void memset(int byteValue, int n) {
    for (int i = 0; i < n; i++) {
      ptr.setByte(this.offset + i, (byte)byteValue);
    }
  }

  @Override
  public void memcpy(Ptr source, int numBytes) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public int memcmp(Ptr other, int numBytes) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public Ptr copyOf(int numBytes) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public boolean isNull() {
    return false;
  }
}

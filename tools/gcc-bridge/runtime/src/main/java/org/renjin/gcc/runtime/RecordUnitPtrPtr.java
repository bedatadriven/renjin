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

public class RecordUnitPtrPtr<T> implements Ptr {

  public static final int BYTES = 4;

  private T[] array;
  private int offset;

  public RecordUnitPtrPtr(T[] array, int offset) {
    this.array = array;
    this.offset = offset;
  }

  @Override
  public Object getArray() {
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
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public Ptr pointerPlus(int bytes) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public boolean getBoolean() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public boolean getBoolean(int offset) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void setBoolean(int offset, boolean value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void setBoolean(boolean value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public byte getByte() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public byte getByte(int offset) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void setByte(byte value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void setByte(int offset, byte value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public short getShort() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public short getShort(int offset) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public short getAlignedShort(int index) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void setShort(short value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void setAlignedShort(int index, short shortValue) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void setShort(int offset, short value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public char getChar() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public char getAlignedChar(int index) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public char getChar(int offset) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void setChar(char value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void setAlignedChar(int index, char value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void setChar(int offset, char value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public double getDouble() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public double getDouble(int offset) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public double getAlignedDouble(int index) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void setDouble(double value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void setDouble(int offset, double value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void setAlignedDouble(int index, double value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public float getFloat() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public float getFloat(int offset) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public float getAlignedFloat(int index) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void setFloat(float value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void setAlignedFloat(int index, float value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void setFloat(int offset, float value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public int getInt() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public int getInt(int offset) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public int getAlignedInt(int index) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void setInt(int value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void setInt(int offset, int value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void setAlignedInt(int index, int value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public long getLong() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public long getLong(int offset) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void setLong(long value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void setLong(int offset, long value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void setAlignedLong(int index, long value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public Ptr getPointer() {
    return new RecordUnitPtr<T>(array[this.offset]);
  }

  @Override
  public Ptr getPointer(int offset) {
    return new RecordUnitPtr<T>(array[this.offset + (offset / BYTES)]);
  }

  @Override
  public Ptr getAlignedPointer(int index) {
    return new RecordUnitPtr<T>(array[this.offset + index]);
  }

  @Override
  public void setPointer(Ptr value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void setPointer(int offset, Ptr value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void setAlignedPointer(int index, Ptr value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public int toInt() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void memset(int byteValue, int n) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void memcpy(Ptr source, int numBytes) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void memmove(Ptr source, int numBytes) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public int memcmp(Ptr other, int numBytes) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public Ptr copyOf(int offset, int numBytes) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public Ptr copyOf(int numBytes) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public boolean isNull() {
    return array == null;
  }

  @Override
  public MethodHandle toMethodHandle() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public int compareTo(Ptr o) {
    throw new UnsupportedOperationException("TODO");
  }
}

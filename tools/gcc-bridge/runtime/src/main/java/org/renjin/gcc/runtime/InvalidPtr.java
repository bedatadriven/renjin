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

import java.lang.invoke.MethodHandle;

/**
 * Created by alex on 25-8-17.
 */
public class InvalidPtr implements Ptr {

  private int value;

  public InvalidPtr(int value) {
    this.value = value;
  }

  @Override
  public Object getArray() {
    return null;
  }

  @Override
  public int getOffset() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public int getOffsetInBytes() {
    return value;
  }

  @Override
  public Ptr realloc(int newSizeInBytes) {
    throw new SegFaultException();
  }

  @Override
  public Ptr pointerPlus(int bytes) {
    int newValue = this.value + bytes;
    if(newValue == 0) {
      return BytePtr.NULL;
    } else {
      return new InvalidPtr(newValue);
    }
  }

  @Override
  public byte getByte() {
    throw new SegFaultException();
  }

  @Override
  public byte getByte(int offset) {
    throw new SegFaultException();
  }

  @Override
  public void setByte(byte value) {
    throw new SegFaultException();
  }

  @Override
  public void setByte(int offset, byte value) {
    throw new SegFaultException();
  }

  @Override
  public short getShort() {
    throw new SegFaultException();
  }

  @Override
  public short getShort(int offset) {
    throw new SegFaultException();
  }

  @Override
  public void setShort(short value) {
    throw new SegFaultException();
  }

  @Override
  public void setShort(int offset, short value) {
    throw new SegFaultException();
  }

  @Override
  public char getChar() {
    throw new SegFaultException();
  }

  @Override
  public char getChar(int offset) {
    throw new SegFaultException();
  }

  @Override
  public void setChar(char value) {
    throw new SegFaultException();
  }

  @Override
  public void setChar(int offset, char value) {
    throw new SegFaultException();
  }

  @Override
  public double getDouble() {
    throw new SegFaultException();
  }

  @Override
  public double getDouble(int offset) {
    throw new SegFaultException();
  }

  @Override
  public double getAlignedDouble(int index) {
    throw new SegFaultException();
  }

  @Override
  public void setDouble(double value) {
    throw new SegFaultException();
  }

  @Override
  public void setDouble(int offset, double value) {
    throw new SegFaultException();
  }

  @Override
  public float getFloat() {
    throw new SegFaultException();
  }

  @Override
  public float getFloat(int offset) {
    throw new SegFaultException();
  }

  @Override
  public void setFloat(float value) {
    throw new SegFaultException();
  }

  @Override
  public void setFloat(int offset, float value) {
    throw new SegFaultException();
  }

  @Override
  public int getInt() {
    throw new SegFaultException();
  }

  @Override
  public int getInt(int offset) {
    throw new SegFaultException();
  }

  @Override
  public int getIntAligned(int index) {
    throw new SegFaultException();
  }

  @Override
  public void setInt(int value) {
    throw new SegFaultException();
  }

  @Override
  public void setInt(int offset, int value) {
    throw new SegFaultException();
  }

  @Override
  public long getLong() {
    throw new SegFaultException();
  }

  @Override
  public long getLong(int offset) {
    throw new SegFaultException();
  }

  @Override
  public void setLong(long value) {
    throw new SegFaultException();
  }

  @Override
  public void setLong(int offset, long value) {
    throw new SegFaultException();
  }

  @Override
  public Ptr getPointer() {
    throw new SegFaultException();
  }

  @Override
  public Ptr getPointer(int offset) {
    throw new SegFaultException();
  }

  @Override
  public void setPointer(Ptr value) {
    throw new SegFaultException();
  }

  @Override
  public void setPointer(int offset, Ptr value) {
    throw new SegFaultException();
  }

  @Override
  public int toInt() {
    return value;
  }

  @Override
  public void memset(int byteValue, int n) {
    throw new SegFaultException();
  }

  @Override
  public void memcpy(Ptr source, int numBytes) {
    throw new SegFaultException();
  }

  @Override
  public int memcmp(Ptr other, int numBytes) {
    throw new SegFaultException();
  }

  @Override
  public Ptr copyOf(int numBytes) {
    throw new SegFaultException();
  }

  @Override
  public boolean isNull() {
    return false;
  }

  @Override
  public MethodHandle toMethodHandle() {
    throw new SegFaultException();
  }

  @Override
  public int compareTo(Ptr o) {
    throw new UnsupportedOperationException("TODO");
  }
}

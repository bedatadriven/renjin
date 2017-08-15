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


public abstract class AbstractPointer implements Pointer {


  @Override
  public void setShort(short value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void setShort(int offset, short value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public short getShort() {
    return getShort(0);
  }

  @Override
  public short getShort(int offset) {
    return (short) ((getByte(offset) << 8) | (getByte(offset + 1) & 0xFF));
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
  public char getChar() {
    return getChar(0);
  }

  @Override
  public char getChar(int offset) {
    return (char) ((getByte(offset) << 8) | (getByte(offset + 1) & 0xFF));
  }

  @Override
  public int getInt() {
    return getInt(0);
  }

  @Override
  public int getInt(int offset) {
    return
         getByte(offset) << 24 |
        (getByte(offset + 1) & 0xFF) << 16 |
        (getByte(offset + 2) & 0xFF) << 8 |
        (getByte(offset + 3) & 0xFF);
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
    return (getByte(offset) & 0xFFL) << 56
      | (getByte(offset + 1) & 0xFFL) << 48
      | (getByte(offset + 2) & 0xFFL) << 40
      | (getByte(offset + 3) & 0xFFL) << 32
      | (getByte(offset + 4) & 0xFFL) << 24
      | (getByte(offset + 5) & 0xFFL) << 16
      | (getByte(offset + 6) & 0xFFL) << 8
      | (getByte(offset + 7) & 0xFFL);
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
  public Pointer getPointer() {
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
  public void setPointer(Pointer value) {
    setPointer(0, value);
  }

  @Override
  public void setChar(int offset, char value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void setDouble(int offset, double doubleValue) {

    long longValue = Double.doubleToRawLongBits(doubleValue);

    for (int i = 0; i < 8; i++) {
      setByte(offset + i, (byte)(longValue & 0xffL));
      longValue >>= 8;
    }
  }

  @Override
  public void setFloat(int offset, float value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void setInt(int offset, int value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void setLong(int offset, long value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void setPointer(int offset, Pointer value) {
    throw new UnsupportedOperationException("TODO");
  }
}


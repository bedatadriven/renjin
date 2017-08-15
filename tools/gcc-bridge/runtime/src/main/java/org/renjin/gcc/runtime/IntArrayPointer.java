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
 * Pointer implementation backed by an array of integers
 */
public class IntArrayPointer implements Pointer {

  private int array[];

  /**
   * Offset of this pointer, in bytes, to the array.
   */
  private int offset;

  public IntArrayPointer(int[] array, int offset) {
    this.array = array;
    this.offset = offset;
  }

  /**
   * Creates a new array-backed virtual pointer
   * @param array an array of integers
   * @param offset the offset, in <strong>elements</strong>, not bytes.
   */
  public static IntArrayPointer fromPair(int[] array, int offset) {
    return new IntArrayPointer(array, offset * 4);
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
  public void setBoolean(boolean value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public byte getByte() {
    return getByte(0);
  }

  @Override
  public byte getByte(int offset) {
    int byteOffset = this.offset + offset;
    int element = byteOffset / 4;
    int shift = (byteOffset % 4) * 8;
    return (byte)(this.array[element] >> shift);
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
  public char getChar() {
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
  public void setDouble(double value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void setDouble(int offset, double value) {
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
  public void setFloat(float value) {
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
  public void setInt(int value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void setInt(int offset, int value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public long getLong() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public int getLong(int offset) {
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
  public Pointer getPointer() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public int getPointer(int offset) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void setPointer(Pointer value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void setPointer(int offset, Pointer value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public Pointer plus(int byteCount) {
    return new IntArrayPointer(this.array, offset + byteCount);
  }


}

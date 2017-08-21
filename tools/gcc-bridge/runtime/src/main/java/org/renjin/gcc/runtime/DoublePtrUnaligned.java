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
 * Pointer implementation backed by a double[] array
 */
public class DoublePtrUnaligned extends AbstractPtr {

  private final double[] array;
  private final int offset;

  public DoublePtrUnaligned(double[] array, int offset) {
    this.array = array;
    this.offset = offset;
  }

  public DoublePtrUnaligned(double value) {
    this(new double[] { value }, 0);
  }

  public static DoublePtrUnaligned fromPair(double[] array, int offset) {
    return new DoublePtrUnaligned(array, offset / 8);
  }

  @Override
  public byte getByte(int offset) {
    int bytes = this.offset + offset;
    int index = bytes / 8;
    double element = array[index];
    long elementBits = Double.doubleToRawLongBits(element);
    int shift = (bytes % 8) * 8;

    return (byte) (elementBits >> shift);
  }

  @Override
  public double getDouble(int offset) {
    int bytes = this.offset + offset;
    if(bytes % 8 == 0) {
      return this.array[offset];
    } else {
      return super.getDouble(offset);
    }
  }

  @Override
  public void setDouble(int offset, double doubleValue) {
    int bytes = this.offset + offset;
    if(bytes % 8 == 0) {
      this.array[bytes / 8] = doubleValue;
    } else {
      super.setDouble(offset, doubleValue);
    }
  }

  @Override
  public void setByte(int offset, byte value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public Ptr getPointer(int offset) {
    throw new UnsupportedOperationException("TODO");
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
  public Ptr realloc(int newSizeInBytes) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public Ptr pointerPlus(int byteCount) {
    return new DoublePtrUnaligned(array, this.offset + byteCount);
  }
}

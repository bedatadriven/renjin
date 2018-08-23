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

public class Double96Ptr extends AbstractPtr {

  public static final int BYTES = 12;

  private double[] array;
  private int offset;

  public Double96Ptr(double[] array, int offset) {
    this.array = array;
    this.offset = offset;
  }

  @Override
  public Object getArray() {
    return array;
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
    if(bytes % BYTES == 0) {
      return new Double96Ptr(array, this.offset + (bytes / BYTES));
    } else {
      return new OffsetPtr(this, bytes);
    }
  }

  @Override
  public byte getByte(int offset) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void setByte(int offset, byte value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public double getReal96() {
    return this.array[this.offset];
  }

  @Override
  public double getReal96(int offset) {
    if(offset % BYTES == 0) {
      return this.array[this.offset + (offset / BYTES)];
    } else {
      return super.getReal96(offset);
    }
  }

  @Override
  public double getAlignedReal96(int index) {
    return this.array[this.offset + index];
  }

  @Override
  public void setReal96(double value) {
    this.array[this.offset] = value;
  }

  @Override
  public void setReal96(int offset, double value) {
    if(offset % BYTES == 0) {
      setAlignedReal96(this.offset + (offset / BYTES), value);
    } else {
      super.setAlignedReal96(offset, value);
    }
  }

  @Override
  public void setAlignedReal96(int index, double value) {
    this.array[this.offset + index] = value;
  }

  @Override
  public int toInt() {
    return offset;
  }

  @Override
  public boolean isNull() {
    return array == null && offset == 0;
  }
}

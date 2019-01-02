/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
package org.renjin.gnur.api;

import org.renjin.gcc.runtime.AbstractPtr;
import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.OffsetPtr;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.sexp.AtomicVector;

import static org.renjin.gcc.runtime.DoublePtr.BYTES;

public class DoubleVectorPtr extends AbstractPtr {
  private AtomicVector vector;
  private int offset;

  public DoubleVectorPtr(AtomicVector vector, int offset) {
    this.vector = vector;
    this.offset = offset;
  }

  @Override
  public Object getArray() {
    return vector;
  }

  @Override
  public final int getOffsetInBytes() {
    return offset * DoublePtr.BYTES;
  }

  @Override
  public Ptr realloc(int newSizeInBytes) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public Ptr pointerPlus(int bytes) {
    if(bytes == 0) {
      return this;
    }
    if(bytes % DoublePtr.BYTES == 0) {
      return new DoubleVectorPtr(this.vector, this.offset + (bytes / DoublePtr.BYTES));
    }
    return new OffsetPtr(this, bytes);
  }

  @Override
  public double getAlignedDouble(int index) {
    return vector.getElementAsDouble(this.offset + index);
  }

  @Override
  public double getDouble(int offset) {
    if(offset % BYTES == 0) {
      return getAlignedDouble(offset / BYTES);
    }
    return super.getDouble(offset);
  }

  @Override
  public byte getByte(int bytes) {
    return getByteViaDouble(bytes);
  }
  @Override
  public void setByte(int offset, byte value) {
    throw new UnsupportedOperationException("Illegal modification of a shared vector! " +
        "Mis-behaving C/C++ code has tried to modify a vector that it should not.");
  }

  @Override
  public int toInt() {
    return offset * DoublePtr.BYTES;
  }

  @Override
  public boolean isNull() {
    return false;
  }
}

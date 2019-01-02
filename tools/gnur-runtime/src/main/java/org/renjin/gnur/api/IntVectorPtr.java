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
import org.renjin.gcc.runtime.IntPtr;
import org.renjin.gcc.runtime.OffsetPtr;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.sexp.AtomicVector;

/**
 * A virtual pointer implementation that points to a read-only R integer vector.
 */
public final class IntVectorPtr extends AbstractPtr {

  private AtomicVector vector;
  private int offset;

  public IntVectorPtr(AtomicVector vector, int offset) {
    this.vector = vector;
    this.offset = offset;
  }

  @Override
  public Object getArray() {
    return vector;
  }

  @Override
  public final int getOffsetInBytes() {
    return offset * IntPtr.BYTES;
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
    if(bytes % IntPtr.BYTES == 0) {
      return new IntVectorPtr(this.vector, this.offset + (bytes / IntPtr.BYTES));
    }
    return new OffsetPtr(this, bytes);
  }

  @Override
  public int getAlignedInt(int index) {
    return vector.getElementAsInt(this.offset + index);
  }

  @Override
  public int getInt(int offset) {
    if(offset % IntPtr.BYTES == 0) {
      return getAlignedInt(offset / IntPtr.BYTES);
    }
    return super.getInt(offset);
  }

  @Override
  public byte getByte(int offset) {
    return getByteViaInt(offset);
  }

  @Override
  public void setByte(int offset, byte value) {
    throw new UnsupportedOperationException("Illegal modification of a shared vector! " +
        "Mis-behaving C/C++ code has tried to modify a vector that it should not.");
  }

  @Override
  public int toInt() {
    return offset * IntPtr.BYTES;
  }

  @Override
  public boolean isNull() {
    return false;
  }
}

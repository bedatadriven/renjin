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

import org.renjin.gcc.runtime.*;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.sexp.StringVector;

public class StringVectorPtr extends AbstractPtr {

  private StringVector vector;
  private int offset;

  public StringVectorPtr(StringVector vector, int offset) {
    this.vector = vector;
    this.offset = offset;
  }

  @Override
  public Object getArray() {
    return vector;
  }

  @Override
  public int getOffsetInBytes() {
    return offset * PointerPtr.BYTES;
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
    if(bytes % PointerPtr.BYTES == 0) {
      return new StringVectorPtr(this.vector, this.offset + (bytes / PointerPtr.BYTES));
    }
    return new OffsetPtr(this, bytes);
  }

  @Override
  public byte getByte(int offset) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public Ptr getAlignedPointer(int index) {
    String element = vector.getElementAsString(offset + index);
    if(element == null) {
      return BytePtr.NULL;
    } else {
      return BytePtr.nullTerminatedString(element, Charsets.UTF_8);
    }
  }

  @Override
  public void setByte(int offset, byte value) {
    throw new UnsupportedOperationException("Illegal modification of a shared vector! " +
        "Mis-behaving C/C++ code has tried to modify a vector that it should not.");
  }

  @Override
  public int toInt() {
    return getOffsetInBytes();
  }

  @Override
  public boolean isNull() {
    return false;
  }
}

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
 * Pointer type that references both primitives and garbage-collected
 * references.
 */
public class MixedPtr extends AbstractPtr {

  private static final int POINTER_BYTES = 4;

  private byte[] primitives;
  private Object[] references;

  private MixedPtr() {
  }

  public static MixedPtr malloc(int bytes) {
    MixedPtr ptr = new MixedPtr();
    ptr.primitives = new byte[bytes];
    ptr.references = new Object[mallocSize(bytes, POINTER_BYTES)];
    return ptr;
  }

  @Override
  public Object getArray() {
    return primitives;
  }

  @Override
  public int getOffsetInBytes() {
    return 0;
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

    return new OffsetPtr(this, bytes);
  }

  @Override
  public byte getByte(int offset) {
    return primitives[offset];
  }

  @Override
  public void setByte(int offset, byte value) {
    primitives[offset] = value;
  }


  @Override
  public Ptr getPointer(int offset) {
    if(offset % POINTER_BYTES == 0) {
      int index = offset / POINTER_BYTES;
      return (Ptr) references[index];
    }
    return BadPtr.INSTANCE;
  }

  @Override
  public final void setPointer(int offset, Ptr value) {
    if(offset % POINTER_BYTES != 0) {
      throw new UnsupportedOperationException("Unaligned pointer storage");
    }
    int index = offset / POINTER_BYTES;
    references[index] = value;
    setInt(index, value.toInt());
  }


  @Override
  public int toInt() {
    return getOffsetInBytes();
  }

  @Override
  public boolean isNull() {
    return false;
  }

  @Override
  public void memcpy(Ptr source, int numBytes) {
    if(source instanceof MixedPtr && numBytes % POINTER_BYTES == 0) {
      MixedPtr ptr = (MixedPtr) source;
      System.arraycopy(ptr.references, 0, references, 0, numBytes / POINTER_BYTES);
      System.arraycopy(ptr.primitives, 0, primitives, 0, numBytes);
    } else if(source instanceof OffsetPtr) {
      throw new UnsupportedOperationException();
      
    } else {
      for (int i = 0; i < numBytes; i++) {
        setByte(i, source.getByte(i));
      }
    }
  }
}

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

import java.util.Arrays;


public class ShortPtr extends AbstractPtr {

  public static final int BYTES = 2;

  public static final ShortPtr NULL = new ShortPtr();
  
  public final short[] array;
  public final int offset;
  
  private ShortPtr() {
    this.array = null;
    this.offset = 0;
  }

  public ShortPtr(short[] array, int offset) {
    this.array = array;
    this.offset = offset;
  }

  public ShortPtr(short... array) {
    this.array = array;
    this.offset = 0;
  }

  public static ShortPtr malloc(int bytes) {
    return new ShortPtr(new short[AbstractPtr.mallocSize(bytes, BYTES)]);
  }

  @Override
  public short[] getArray() {
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
    return new ShortPtr(Realloc.realloc(array, offset, newSizeInBytes / 2));
  }

  @Override
  public Ptr pointerPlus(int bytes) {
    return new ShortPtr(array, offset + (bytes / 2));
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
  public int toInt() {
    return offset * BYTES;
  }

  @Override
  public boolean isNull() {
    return array == null && offset != 0;
  }

  public short unwrap() {
    return array[offset];
  }

  @Override
  public String toString() {
    return offset + "+" + Arrays.toString(array);
  }


  public static void memset(short[] array, int offset, int value, int length) {
    throw new UnsupportedOperationException("TODO");
  }

  public static short memset(int byteValue) {
    throw new UnsupportedOperationException("TODO");
  }

  public static ShortPtr cast(Object voidPointer) {
    if(voidPointer instanceof MallocThunk) {
      return ((MallocThunk) voidPointer).shortPtr();
    }
    if(voidPointer == null) {
      return NULL;
    }
    return (ShortPtr) voidPointer;  
  }
}

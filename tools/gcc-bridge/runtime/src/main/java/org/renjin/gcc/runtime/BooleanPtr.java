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
package org.renjin.gcc.runtime;


public class BooleanPtr extends AbstractPtr implements Ptr {

  public static final BooleanPtr NULL = new BooleanPtr();

  public final boolean[] array;
  public final int offset;


  public static BooleanPtr malloc(int bytes) {
    return new BooleanPtr(new boolean[bytes]);
  }

  private BooleanPtr() {
    array = null;
    offset = 0;
  }
  
  public BooleanPtr(boolean[] array, int offset) {
    this.array = array;
    this.offset = offset;
  }

  public BooleanPtr(boolean... values) {
    this.array = values;
    this.offset = 0;
  }

  public boolean unwrap() {
    return array[offset];
  }

  @Override
  public boolean[] getArray() {
    return array;
  }

  @Override
  public int getOffset() {
    return offset;
  }

  @Override
  public int getOffsetInBytes() {
    return offset;
  }

  @Override
  public BooleanPtr realloc(int newSizeInBytes) {
    return new BooleanPtr(Realloc.realloc(array, offset, newSizeInBytes));
  }

  @Override
  public Ptr pointerPlus(int bytes) {
    return new BooleanPtr(array, offset + bytes);
  }

  @Override
  public byte getByte(int offset) {
    return array[offset] ? (byte)1 : (byte)0;
  }

  @Override
  public void setByte(int offset, byte value) {
    array[offset] = (value != 0);
  }

  @Override
  public int toInt() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public boolean isNull() {
    return array == null && offset == 0;
  }

  public static BooleanPtr cast(Object voidPointer) {
    if(voidPointer instanceof MallocThunk) {
      return ((MallocThunk) voidPointer).booleanPtr();
    }
    return (BooleanPtr) voidPointer;
  }
}

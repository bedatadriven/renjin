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


import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class FunctionPtr extends AbstractPtr {

  private static MethodHandle BAD_HANDLE;

  public static final int BYTES = 4;

  private MethodHandle[] array;
  private int offset;

  public FunctionPtr(MethodHandle[] array, int offset) {
    this.array = array;
    this.offset = offset;
  }

  @Override
  public Object getArray() {
    return array;
  }

  @Override
  public int getOffsetInBytes() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public Ptr realloc(int newSizeInBytes) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public Ptr pointerPlus(int bytes) {
    if(bytes % BYTES == 0) {
      return new FunctionPtr(array, offset + (bytes / BYTES));
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
  public int toInt() {
    return offset * BYTES;
  }

  @Override
  public boolean isNull() {
    return array == null;
  }

  @Override
  public Ptr getPointer(int offset) {
    if(offset % BYTES == 0) {
      return new FunctionPtr1(array[this.offset + offset / BYTES]);
    } else {
      throw new UnsupportedOperationException("Unaligned pointer access");
    }
  }

  @Override
  public MethodHandle toMethodHandle() {
    return array[0];
  }

  public static void invalidFunction() {
    throw new RuntimeException("Not a function pointer. SEGFAULT.");
  }

  public static MethodHandle getBadHandle() {
    if(BAD_HANDLE == null) {
      try {
        BAD_HANDLE = MethodHandles.publicLookup().findStatic(FunctionPtr.class, "invalidFunction", MethodType.methodType(void.class));
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw new IllegalStateException(e);
      }
    }
    return BAD_HANDLE;
  }
}

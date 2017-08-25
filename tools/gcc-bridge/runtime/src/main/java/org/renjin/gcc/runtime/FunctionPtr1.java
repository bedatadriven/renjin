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

import java.lang.invoke.MethodHandle;

/**
 * Wraps a single MethodHandle
 */
public class FunctionPtr1 extends AbstractPtr {

  private MethodHandle methodHandle;

  public static Ptr malloc(MethodHandle methodHandle) {
    return new FunctionPtr1(methodHandle);
  }

  public FunctionPtr1(MethodHandle methodHandle) {
    this.methodHandle = methodHandle;
  }

  @Override
  public Object getArray() {
    throw new UnsupportedOperationException("TODO");
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
    return new OffsetPtr(this, bytes);
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
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public boolean isNull() {
    return methodHandle == null;
  }

  @Override
  public MethodHandle toMethodHandle() {
    return methodHandle;
  }
}

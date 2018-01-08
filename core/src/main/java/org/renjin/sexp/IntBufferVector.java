/**
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
package org.renjin.sexp;

import java.nio.IntBuffer;

public class IntBufferVector extends IntVector {

  private final IntBuffer buffer;
  private int length;

  public IntBufferVector(IntBuffer buffer, int length) {
    this.buffer = buffer;
    this.length = length;
  }

  public IntBufferVector(IntBuffer buffer, int length, AttributeMap attributes) {
    super(attributes);
    this.buffer = buffer;
    this.length = length;
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  public int getElementAsInt(int i) {
    return buffer.get(i);
  }

  @Override
  public boolean isConstantAccessTime() {
    return true;
  }
  
  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new IntBufferVector(buffer, length, attributes);
  }

  /**
   * Returns the underlying {@code IntBuffer} backing this vector. The 
   * returned buffer <strong>absolutely should not be modified!</strong>
   */
  public IntBuffer toIntBufferUnsafe() {
    return buffer;
  }

  @Override
  public void copyTo(double[] array, int offset, int length) {
    for (int i = 0; i < length; i++) {
      int intValue = buffer.get(i);
      if(intValue == IntVector.NA) {
        array[offset + i] = DoubleVector.NA;
      } else {
        array[offset + i] = intValue;
      }
    }
  }
}

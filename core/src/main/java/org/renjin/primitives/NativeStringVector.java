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
package org.renjin.primitives;

import org.renjin.gcc.runtime.ObjectPtr;
import org.renjin.gcc.runtime.PointerPtr;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.gcc.runtime.Stdlib;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.StringVector;

public class NativeStringVector extends StringVector {
  
  private Ptr[] array;
  private int offset;
  private int length;

  public NativeStringVector(Ptr[] array, int offset, int length, AttributeMap attributes) {
    super(attributes);
    this.array = array;
    this.offset = offset;
    this.length = length;
  }

  public NativeStringVector(ObjectPtr ptr, AttributeMap attributes) {
    super(attributes);
    this.array = (Ptr[])ptr.array;
    this.offset = ptr.offset;
    this.length = ptr.array.length;
  }

  public NativeStringVector(PointerPtr ptr, AttributeMap attributes) {
    super(attributes);
    this.array = (Ptr[]) ptr.getArray();
    this.offset = 0;
    this.length = this.array.length;
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  protected StringVector cloneWithNewAttributes(AttributeMap attributes) {
    return new NativeStringVector(array, offset, length, attributes);
  }

  @Override
  public String getElementAsString(int index) {
    Ptr string = array[offset + index];
    if(string == null) {
      return null;
    } else {
      return Stdlib.nullTerminatedString(string);
    }
  }

  @Override
  public boolean isConstantAccessTime() {
    return true;
  }
}

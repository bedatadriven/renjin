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
package org.renjin.embed;

import org.renjin.sexp.*;
import org.rosuda.JRI.Rengine;

/**
 * Wraps a pointer to a GNU R INTSXP.
 *
 * <p>Integer values are retrieved when first used.</p>
 */
class IntVectorWrapper extends IntVector implements WrappedREXP {

  private final Rengine engine;
  private final long pointer;

  private int[] values;

  public IntVectorWrapper(Rengine engine, long pointer, AttributeMap attributeMap) {
    super(attributeMap);
    this.engine = engine;
    this.pointer = pointer;
  }


  public int length() {
    force();
    return values.length;
  }

  private void force() {
    if(values == null) {
      values = engine.rniGetIntArray(pointer);
    }
  }

  public int getElementAsInt(int i) {
    force();
    return values[i];
  }

  public boolean isConstantAccessTime() {
    return true;
  }

  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    if(values == null) {
      return new IntVectorWrapper(engine, pointer, attributes);
    } else {
      return IntArrayVector.unsafe(values, attributes);
    }
  }

  @Override
  public long getHandle() {
    return pointer;
  }
}

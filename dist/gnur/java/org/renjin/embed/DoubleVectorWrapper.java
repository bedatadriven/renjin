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
package org.renjin.embed;

import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.SEXP;
import org.rosuda.JRI.Rengine;

/**
 * Wraps a pointer to a GNU R SEXP.
 *
 * <p>The values of the vector are only retrieved when first accessed.</p>
 */
class DoubleVectorWrapper extends DoubleVector implements WrappedREXP {

  private final Rengine engine;
  private final long pointer;
  private double[] values;


  public DoubleVectorWrapper(Rengine engine, long pointer, AttributeMap attributeMap) {
    super(attributeMap);
    this.engine = engine;
    this.pointer = pointer;
  }

  private void force() {
    if(values == null) {
      values = engine.rniGetDoubleArray(pointer);
    }
  }


  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    if(values == null) {
      return new DoubleVectorWrapper(engine, pointer, attributes);
    } else {
      return DoubleArrayVector.unsafe(values, attributes);
    }
  }

  @Override
  public double getElementAsDouble(int index) {
    force();
    return values[index];
  }

  @Override
  public int length() {
    force();
    return values.length;
  }

  @Override
  public boolean isConstantAccessTime() {
    return true;
  }

  @Override
  public long getHandle() {
    return pointer;
  }
}

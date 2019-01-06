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
package org.renjin.embed;

import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.StringArrayVector;
import org.renjin.sexp.StringVector;
import org.rosuda.JRI.Rengine;


public class StringVectorWrapper extends StringVector implements WrappedREXP {

  private final Rengine engine;
  private final long pointer;

  private String[] values;

  public StringVectorWrapper(Rengine engine, long pointer, AttributeMap attributeMap) {
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
      values = engine.rniGetStringArray(pointer);
    }
  }

  @Override
  public String getElementAsString(int index) {
    force();
    return values[index];
  }

  public boolean isConstantAccessTime() {
    return true;
  }

  protected StringVector cloneWithNewAttributes(AttributeMap attributes) {
    if(values == null) {
      return new StringVectorWrapper(engine, pointer, attributes);
    } else {
      return new StringArrayVector(values, attributes);
    }
  }

  @Override
  public long getHandle() {
    return pointer;
  }
}

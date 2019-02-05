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
package org.renjin.gnur.api;

import org.renjin.gcc.runtime.PointerPtr;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.StringVector;

import java.util.Arrays;

/**
 * StringVector implementation backed by BytePtrs
 */
public class GnuStringVector extends StringVector {
  
  private GnuCharSexp[] values;

  public GnuStringVector(String string) {
    this(GnuCharSexp.valueOf(string));
  }
  
  public GnuStringVector(GnuCharSexp... values) {
    this(values, AttributeMap.EMPTY);
  }
  
  public GnuStringVector(GnuCharSexp[] values, AttributeMap attributes) {
    super(attributes);
    this.values = values;
  }

  public static GnuStringVector copyOf(StringVector vector) {
    if(vector instanceof GnuStringVector) {
      return new GnuStringVector(Arrays.copyOf(((GnuStringVector) vector).values, vector.length()));
    } else {
      GnuCharSexp values[] = new GnuCharSexp[vector.length()];
      for (int i = 0; i < values.length; i++) {
        values[i] = GnuCharSexp.valueOf(vector.getElementAsString(i));
      }
      return new GnuStringVector(values);
    }
  }

  @Override
  public int length() {
    return values.length;
  }

  @Override
  protected StringVector cloneWithNewAttributes(AttributeMap attributes) {
    return new GnuStringVector(values, attributes);
  }

  @Override
  public String getElementAsString(int index) {
    GnuCharSexp value = values[index];
    if(value == GnuCharSexp.NA_STRING) {
      return null;
    } else {
      return value.getValue().nullTerminatedString();
    }
  }

  @Override
  public boolean isElementNA(int index) {
    return values[index] == null;
  }

  @Override
  public boolean isConstantAccessTime() {
    return true;
  }

  public void set(int index, GnuCharSexp charValue) {
    values[index] = charValue;
  }

  public GnuCharSexp getElementAsCharSexp(int index) {
    return values[index];
  }

}

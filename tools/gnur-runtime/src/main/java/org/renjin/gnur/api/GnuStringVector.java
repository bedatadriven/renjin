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
package org.renjin.gnur.api;

import org.renjin.gcc.runtime.BytePtr;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.StringVector;

/**
 * StringVector implementation backed by BytePtrs
 */
public class GnuStringVector extends StringVector {
  
  private BytePtr[] values;

  public GnuStringVector(String string) {
    this(BytePtr.nullTerminatedString(string, Charsets.UTF_8));
  }
  
  public GnuStringVector(BytePtr... values) {
    this(values, AttributeMap.EMPTY);
  }
  
  public GnuStringVector(BytePtr[] values, AttributeMap attributes) {
    super(attributes);
    this.values = values;
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
    BytePtr value = values[index];
    if(value == null) {
      return null;
    } else {
      return value.nullTerminatedString();
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
    values[index] = charValue.getValue();
  }
}

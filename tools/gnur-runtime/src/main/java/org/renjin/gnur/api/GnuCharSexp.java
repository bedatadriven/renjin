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
import org.renjin.sexp.AbstractSEXP;
import org.renjin.sexp.SexpVisitor;
import org.renjin.sexp.StringVector;

/**
 * Internal character SEXP
 */
public class GnuCharSexp extends AbstractSEXP {

  public static final GnuCharSexp NA_STRING = new GnuCharSexp(new byte[] { 'N', 'A', 0 });
  public static final GnuCharSexp BLANK_STRING = new GnuCharSexp(new byte[] { (int)0 });

  private byte[] value;

  public GnuCharSexp(byte[] value) {
    this.value = value;
  }

  public static GnuCharSexp valueOf(String value) {
    if(StringVector.isNA(value)) {
      return NA_STRING;
    } else if(value.isEmpty()) {
      return BLANK_STRING;
    } else {
      return new GnuCharSexp(BytePtr.nullTerminatedString(value, Charsets.UTF_8).array);
    }
  }

  @Override
  public String getTypeName() {
    return "char";
  }

  @Override
  public void accept(SexpVisitor visitor) {
  }

  public BytePtr getValue() {
    return new BytePtr(value, 0);
  }
}

/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${$file.lastModified.year} BeDataDriven Groep B.V. and contributors
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

package org.renjin.primitives.io.scan;

import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.StringArrayVector;
import org.renjin.sexp.StringVector;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

class StringReader implements AtomicReader {

  private final Charset charset;
  private CharsetDecoder charsetDecoder;
  private StringArrayVector.Builder builder;

  public StringReader() {
    this(Charsets.UTF_8);
  }

  public StringReader(Charset charset) {
    this.charset = charset;
    this.builder = new StringVector.Builder();
  }

  @Override
  public void readUTF8(byte[] buffer, int length) {
    builder.add(new String(buffer, 0, length, charset));
  }

  @Override
  public AtomicVector build() {
    return builder.build();
  }
}

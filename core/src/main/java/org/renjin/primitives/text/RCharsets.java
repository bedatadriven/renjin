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
package org.renjin.primitives.text;

import org.renjin.eval.EvalException;
import org.renjin.repackaged.guava.base.Charsets;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

public final class RCharsets {

  /**
   * Returns a Java {@link Charset} object for an R encoding name.
   *
   * @throws EvalException if the encoding is unsupported.
   */
  public static Charset getByName(String name) {
    if("UTF8".equals(name) || "unknown".equals(name) || name.isEmpty()) {
      return Charsets.UTF_8;
    } else if("native.enc".equals(name)) {
      return Charsets.UTF_8;
    } else {
      try {
        return Charset.forName(name);
      } catch (UnsupportedCharsetException e) {
        throw new EvalException("Unsupported encoding: " + name);
      }
    }
  }
}

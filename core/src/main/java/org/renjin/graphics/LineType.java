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
package org.renjin.graphics;

import org.renjin.sexp.IntVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringArrayVector;
import org.renjin.sexp.StringVector;

public enum LineType {
  BLANK(0),
  SOLID(1),
  DASHED(2),
  DOTTED(3),
  DOTDASH(4),
  LONGDASH(5),
  TWODASH(6);

  private final int code;

  LineType(int code) {
    this.code = code;
  }

  public int getCode() {
    return code;
  }

  public static LineType valueOf(SEXP exp) {
    return valueOf(exp);
  }

  public static LineType valueOf(SEXP exp, int elementIndex) {
    if(exp instanceof StringVector) {
      return valueOf(((StringVector) exp).getElementAsString(elementIndex).toUpperCase());
    } else if(exp instanceof IntVector) {
      return valueOf(((IntVector) exp).getElementAsInt(elementIndex));
    } else {
      throw new IllegalArgumentException("" + exp);
    }
  }

  private static LineType valueOf(int code) {
    for(LineType lineType : values()) {
      if(lineType.getCode() == code) {
        return lineType;
      }
    }
    throw new IllegalArgumentException("Unknown LineType code: " + code);
  }

  public StringVector toExpression() {
    return new StringArrayVector(name().toLowerCase());
  }
}

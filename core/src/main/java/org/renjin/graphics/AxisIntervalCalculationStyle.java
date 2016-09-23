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

import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringArrayVector;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Vector;

/**
 * The style of axis interval calculation. Only {@code REGULAR} and
 * {@code INTERNAL} styles are implemented in R
 */                                                            
public enum AxisIntervalCalculationStyle {
  /**
   * first extends the data range by 4 percent at each end and then finds an axis with pretty labels that fits within the extended range.
   */
  REGULAR,

  /**
   * just finds an axis with pretty labels that fits within the original data range.
   */
  INTERNAL,

  /**
   * finds an axis with pretty labels within which the original data range fits.
   */
  STANDARD,

  /**
   * s like style "s", except that it is also ensures that there is room for plotting symbols within the bounding box.
   */
  EXTENDED,

  /**
   * specifies that the current axis should be used on subsequent plots.
   */
  DIRECT;


  public static AxisIntervalCalculationStyle fromExp(SEXP exp) {
    if(exp instanceof Vector) {
      switch(((Vector) exp).getElementAsString(0).charAt(0)) {
        case 'i':
          return AxisIntervalCalculationStyle.INTERNAL;
        case 'r':
          return AxisIntervalCalculationStyle.REGULAR;
      }
    }
    throw new IllegalArgumentException("" + exp);
  }

  public StringVector toExp() {
    return new StringArrayVector(name().substring(0,1).toLowerCase());
  }
}

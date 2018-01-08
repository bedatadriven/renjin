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
package org.renjin.primitives.vector;

import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;


public class BinCodeVector extends IntVector {

  private Vector x;
  private double[] breaks;
  private boolean left;
  private boolean includeBorder;

  public BinCodeVector(Vector x, double breaks[], boolean left, boolean includeBorder,
                        AttributeMap attributes) {
    super(attributes);
    this.x = x;
    this.breaks = breaks;
    this.left = left;
    this.includeBorder = includeBorder;
  }

  @Override
  public int length() {
    return x.length();
  }

  @Override
  public int getElementAsInt(int i) {
    if(!x.isElementNA(i)) {
      int lo = 0;
      int hi = breaks.length - 1;
      double xi = x.getElementAsDouble(i);
      boolean outOfBounds =
              xi < breaks[lo] ||
                      breaks[hi] < xi ||
                      (xi == breaks[left ? hi : lo] && !includeBorder);

      if(!outOfBounds) {
        while(hi - lo >= 2) {
          int code = (hi + lo) / 2;
          if(xi > breaks[code] || (left && xi == breaks[code])) {
            lo = code;
          } else {
            hi = code;
          }
        }
        return lo+1;
      }
    }
    return IntVector.NA;
  }

  @Override
  public boolean isConstantAccessTime() {
    return true;
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new BinCodeVector(x, breaks, left, includeBorder, attributes);
  }
}

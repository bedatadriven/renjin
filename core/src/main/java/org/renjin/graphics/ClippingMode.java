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

import org.renjin.sexp.Logical;
import org.renjin.sexp.LogicalArrayVector;
import org.renjin.sexp.LogicalVector;
import org.renjin.sexp.SEXP;


public enum ClippingMode {
  
  PLOT(Logical.FALSE),
  FIGURE(Logical.TRUE),
  DEVICE(Logical.NA);

  private final Logical logicalValue;

  ClippingMode(Logical logicalValue) {
    this.logicalValue = logicalValue;
  }

  public LogicalVector toExp() {
    return new LogicalArrayVector(logicalValue);
  }

  public static ClippingMode fromExp(SEXP exp) {
    if(exp instanceof LogicalVector) {
      switch(((LogicalVector) exp).getElementAsLogical(0)) {
        case FALSE:
          return PLOT;
        case TRUE:
          return FIGURE;
        case NA:
          return DEVICE;
      }
    }
    throw new IllegalArgumentException(exp.toString());
  }
}

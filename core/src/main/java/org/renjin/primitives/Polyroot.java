/*
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
package org.renjin.primitives;

import org.renjin.appl.Appl;
import org.renjin.eval.EvalException;
import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.IntPtr;
import org.renjin.invoke.annotations.Internal;
import org.renjin.sexp.*;

/**
 * Implements the polyroot() primitive
 */
public class Polyroot {


  @Internal
  public static SEXP polyroot(AtomicVector z) {

    if(!(z instanceof ComplexVector ||
         z instanceof DoubleVector ||
         z instanceof IntVector ||
         z instanceof LogicalVector)) {
      throw new EvalException("unsupported vector type: " + z.getTypeName());
    }

    int n = z.length();

    int degree = 0;
    for(int i = 0; i < n; i++) {
      double re = z.getElementAsDouble(i);
      double im = z.getElementAsComplexIm(i);
      if(re != 0.0 || im != 0.0) {
        degree = i;
      }
    }
    n = degree + 1; /* omit trailing zeroes */
    if(degree >= 1) {
      double rr[] = new double[n];
      double ri[] = new double[n];
      double zr[] = new double[n];
      double zi[] = new double[n];

      for(int i = 0 ; i < n ; i++) {
        double re = z.getElementAsDouble(i);
        double im = z.getElementAsComplexIm(i);

        if(!Double.isFinite(re) || !Double.isFinite(im)) {
          throw new EvalException("invalid polynomial coefficient");
        }
        zr[degree - i] = re;
        zi[degree - i] = im;
      }
      IntPtr fail = new IntPtr(0);
      Appl.cpolyroot(
            new DoublePtr(zr), new DoublePtr(zi),
            new IntPtr(degree),
            new DoublePtr(rr), new DoublePtr(ri),
            fail);

      if(fail.unwrap() != 0) {
        throw new EvalException("root finding code failed");
      }

      ComplexArrayVector.Builder result = new ComplexArrayVector.Builder(degree);
      for (int i = 0; i < degree; i++) {
        result.set(i, rr[i], ri[i]);
      }
      return result.build();

    } else {
      return new ComplexArrayVector();
    }
  }
}

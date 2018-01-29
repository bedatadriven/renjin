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

package org.renjin.primitives.matrix;


import com.github.fommil.netlib.BLAS;
import org.renjin.primitives.vector.DeferredFunction;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.DoubleVector;

public class SymmetricalCrossProduct implements DeferredFunction {
  @Override
  public String getComputationName() {
    return "symcrossprod";
  }

  @Override
  public DoubleVector compute(AtomicVector[] operands) {
    double[] x = operands[0].toDoubleArray();
    int nrx = 0;
    int ncx = 0;
    int ncy = 0;

    String trans = "T";
    String uplo = "U";
    double one = 1.0, zero = 0.0;

    double[] z = new double[ncx * ncy];

    int i, j;
    if (nrx > 0 && ncx > 0) {
      BLAS.getInstance().dsyrk(uplo, trans, ncx, nrx, one, x, nrx, zero, z, ncx);

      for (i = 1; i < ncx; i++) {
        for (j = 0; j < i; j++) {
          z[i + ncx * j] = z[j + ncx * i];
        }
      }
    }
    return z;
  }

  @Override
  public int computeLength(AtomicVector[] operands) {
    throw new UnsupportedOperationException("TODO");
  }
}

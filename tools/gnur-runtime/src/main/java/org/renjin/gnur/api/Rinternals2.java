/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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

import org.renjin.eval.EvalException;
import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.IntPtr;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.sexp.*;

public class Rinternals2 {

  public static Ptr INTEGER(SEXP x) {

    if(x instanceof IntArrayVector) {
      return new IntPtr(((IntArrayVector) x).toIntArrayUnsafe());
    }
    if(x instanceof LogicalArrayVector) {
      return new IntPtr(((LogicalArrayVector) x).toIntArrayUnsafe());
    }

    if(x == Null.INSTANCE) {
      return new IntPtr(0);
    }

    if(x instanceof AtomicVector) {
      return new IntVectorPtr(((AtomicVector) x), 0);
    }

    throw new EvalException("INTEGER(): expected integer vector, found %s", x.getTypeName());
  }

  public static Ptr REAL(SEXP x) {
    if(x instanceof DoubleArrayVector) {
      // Return the array backing the double vector
      // This is inherently unsafe because the calling code can modify the contents of the array
      // and potentially break the contract of immutability of DoubleVector, but the GNU R API
      // imposes essentially the same contract. We are, however, at the mercy of the C code observing
      // this contract.
      return new DoublePtr(((DoubleArrayVector) x).toDoubleArrayUnsafe());
    } else if(x instanceof DoubleVector) {
      return new DoubleVectorPtr((AtomicVector) x,0);
    } else {
      throw new EvalException("REAL(): expected numeric vector, found %s", x.getTypeName());
    }
  }
}

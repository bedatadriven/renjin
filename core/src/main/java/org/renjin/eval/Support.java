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
package org.renjin.eval;

import org.renjin.invoke.annotations.CompilerSpecialization;
import org.renjin.serialization.RDataReader;
import org.renjin.sexp.*;

import java.io.IOException;
import java.io.InputStream;

/**
 * Provides helper methods for compiled code
 */
public class Support {


  public static SEXP[] loadPool(String resourceName) throws IOException {
    InputStream poolInput = Support.class.getResourceAsStream("/" + resourceName);
    if(poolInput == null) {
      throw new IOException("Could not open SEXP pool " + resourceName);
    }
    ListVector vector;
    try(RDataReader reader = new RDataReader(poolInput)) {
      vector = (ListVector) reader.readFile();
    }
    return vector.toArrayUnsafe();
  }

  /**
   * Tests whether an SEXP is true.
   *
   */
  @CompilerSpecialization
  public static int test(SEXP sexp) {
    if (sexp instanceof AtomicVector && !(sexp instanceof StringVector)) {
      AtomicVector vector = (AtomicVector) sexp;
      if (vector.length() == 0) {
        return LogicalVector.NA;
      } else {
        return vector.getElementAsRawLogical(0);
      }
    } else {
      throw new EvalException("invalid type used in logical operator");
    }
  }

  public boolean test(double x) {
    if(Double.isNaN(x)) {
      throw new EvalException("argument is not interpretable as logical");
    }
    return x != 0d;
  }


  /**
   * Converts an SEXP expression to a boolean value used to branch during IF statments.
   *
   * @throws EvalException if the vector is the wrong type, NA, or zero-length
   */
  public static boolean testNoNA(SEXP sexp) {
    if (sexp instanceof AtomicVector && !(sexp instanceof StringVector)) {
      AtomicVector vector = (AtomicVector) sexp;
      if (vector.length() == 0) {
        throw new EvalException("argument is of length zero in if() statement");
      } else {
        return vector.getElementAsInt(0) != 0;
      }
    } else {
      throw new EvalException("invalid type used in || or &&");
    }
  }

  public static void checkNotNA(int branchValue) {
    if(branchValue == IntVector.NA) {
      throw new EvalException("if() condition is an empty vector, NA, or an invalid type");
    }
  }

}

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

package org.renjin.eval;

import org.renjin.primitives.io.serialization.RDataReader;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.SEXP;

import java.io.IOException;
import java.io.InputStream;

/**
 * Provides helper methods for compiled code
 */
public class Support {
  public static final String[] UNNAMED_ARGUMENTS_0 = {};
  public static final String[] UNNAMED_ARGUMENTS_1 = new String[1];
  public static final String[] UNNAMED_ARGUMENTS_2 = new String[2];
  public static final String[] UNNAMED_ARGUMENTS_3 = new String[3];
  public static final String[] UNNAMED_ARGUMENTS_4 = new String[3];
  public static final String[] UNNAMED_ARGUMENTS_5 = new String[4];

  public static boolean test(SEXP sexp) {
    throw new UnsupportedOperationException("TODO");
  }

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

}

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
package org.renjin.primitives.matrix;

import org.renjin.parser.NumericLiterals;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.StringVector;


public class StringMatrixBuilder extends AbstractMatrixBuilder<StringVector.Builder, StringVector>
    implements MatrixBuilder {

  public StringMatrixBuilder(int nrows, int ncols) {
    super(StringVector.VECTOR_TYPE, nrows, ncols);
  }

  @Override
  public void setValue(int row, int col, double value) {
    if(DoubleVector.isNA(value)) {
      setValue(row, col, StringVector.NA);
    } else {
      setValue(row, col, NumericLiterals.toString(value));
    }
  }

  @Override
  public void setValue(int row, int col, int value) {
    if(IntVector.isNA(value)) {
      setValue(row, col, StringVector.NA);
    } else {
      setValue(row, col, NumericLiterals.format(value));
    }
  }
  
  public void setValue(int row, int col, String value) {
    builder.set(computeIndex(row, col), value);
  }
 
}

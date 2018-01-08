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
package org.renjin.primitives.combine;

import org.renjin.eval.Context;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

import java.util.List;

/**
 * Special function to do rbind and use objectnames as rownames
 */
public class RowBindFunction extends AbstractBindFunction {

  public RowBindFunction() {
    super("rbind", MatrixDim.ROW);
  }

  @Override
  protected SEXP apply(Context context, List<BindArgument> bindArguments) {
   
    // establish the number of columns
    // First check actual matrices
    int columns = computeColumnCount(context, bindArguments);

    // Zero-length vectors like integer(0) are ONLY included
    // if the output has zero columns. Otherwise they are discarded.
    if(columns > 0) {
      bindArguments = excludeZeroLengthVectors(bindArguments);
    }

    int rows = countRowOrCols(bindArguments, MatrixDim.ROW);

    // Construct the result
    Vector.Builder vectorBuilder = builderForCommonType(bindArguments);
    Matrix2dBuilder builder = new Matrix2dBuilder(vectorBuilder, rows, columns);
    for (int j = 0; j != columns; ++j) {
      for (BindArgument argument : bindArguments) {
        for (int i = 0; i != argument.getRows(); ++i) {
          builder.addFrom(argument, i, j);
        }
      }
    }

    AtomicVector rowNames = combineDimNames(bindArguments, MatrixDim.ROW);
    AtomicVector colNames = dimNamesFromLongest(bindArguments, MatrixDim.COL, columns);

    if(allZeroLengthVectors(bindArguments)) {
      // This doesn't seem like a great choice, but reproduce
      // behavior of GNU R
      builder.setDimNames(Null.INSTANCE, Null.INSTANCE);

    } else if(rowNames != Null.INSTANCE || colNames != Null.INSTANCE) {
      builder.setDimNames(rowNames, colNames);
    }

    return builder.build();
  }

  private int computeColumnCount(Context context, List<BindArgument> bindArguments) {
    int columns = findCommonMatrixDimLength(bindArguments, MatrixDim.COL);

    // if there are no actual matrices, then use the longest 
    // vector length as the number of columns
    if (columns == -1) {
      columns = findMaxLength(bindArguments);
    }

    // now check that all vectors lengths are multiples of the column length
    warnIfVectorLengthsAreNotMultiple(context, bindArguments, columns);
    return columns;
  }
}


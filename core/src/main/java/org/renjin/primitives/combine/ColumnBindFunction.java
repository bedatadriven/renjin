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
 * Special function to do cbind and use objectnames as columnnames
 */
public class ColumnBindFunction extends AbstractBindFunction {

  public ColumnBindFunction() {
    super("cbind", MatrixDim.COL);
  }

  public SEXP apply(Context context, List<BindArgument> bindArguments) {
    // establish the number of rows
    int rows = computeRowCount(bindArguments, context);

    // Zero-length vectors like integer(0) are ONLY included
    // if the output has zero rows. Otherwise they are discarded.
    if(rows > 0) {
      bindArguments = excludeZeroLengthVectors(bindArguments);
    }

    // now calculate the number of columns and 
    // determine which arguments we're actually keeping
    int columns = countRowOrCols(bindArguments, MatrixDim.COL);

    // Const
    Vector.Builder vectorBuilder = builderForCommonType(bindArguments);
    Matrix2dBuilder builder = new Matrix2dBuilder(vectorBuilder, rows, columns);
    for (BindArgument argument : bindArguments) {
      if(!argument.isZeroLength()) {
        for (int j = 0; j != argument.getCols(); ++j) {
          for (int i = 0; i != rows; ++i) {
            builder.addFrom(argument, i, j);
          }
        }
      }
    }

    AtomicVector rowNames = dimNamesFromLongest(bindArguments, MatrixDim.ROW, rows);
    AtomicVector colNames = combineDimNames(bindArguments, MatrixDim.COL);

    if(allZeroLengthVectors(bindArguments)) {
      // This doesn't seem like a great choice, but reproduce
      // behavior of GNU R
      builder.setDimNames(Null.INSTANCE, Null.INSTANCE);
    
    } else if(rowNames != Null.INSTANCE || colNames != Null.INSTANCE) {
      builder.setDimNames(rowNames, colNames);
    }

    return builder.build();
  }

  private int computeRowCount(List<BindArgument> bindArguments, Context context) {
    // First check the provided matrices. If there any matrices present,
    // they determine the output's row count.
    int rows = findCommonMatrixDimLength(bindArguments, MatrixDim.ROW);

    // if there are no actual matrices, 
    // then use the longest vector length as the number of rows
    if (rows == -1) {
      rows = findMaxLength(bindArguments);
    }

    warnIfVectorLengthsAreNotMultiple(context, bindArguments, rows);
    return rows;
  }

}
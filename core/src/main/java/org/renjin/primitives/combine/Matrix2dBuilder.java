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

import org.renjin.sexp.*;

/**
 * Builds a two-dimensional matrix using an underlying {@link Vector.Builder}
 */
class Matrix2dBuilder {
  private final Vector.Builder builder;
  private final int rows;
  private final int cols;
  private int count = 0;

  public Matrix2dBuilder(Vector.Builder builder, int rows, int cols) {
    this.builder = builder;
    this.rows = rows;
    this.cols = cols;
  }

  public void addFrom(BindArgument argument, int rowIndex, int colIndex) {
    int recycledColIndex = colIndex % argument.getCols();
    int recycledRowIndex = rowIndex % argument.getRows();
    builder.setFrom(count, argument.getVector(), recycledColIndex * argument.getRows() + recycledRowIndex);
    count++;
  }

  public void setDimNames(AtomicVector rowNames, AtomicVector colNames) {
    // Suprisingly enough, on GNU R, cbind/rbind produces dimnames = list(NULL, NULL)
    builder.setAttribute(Symbols.DIMNAMES, new ListVector(rowNames, colNames));
  }

  public Vector build() {
    return builder.setAttribute(Symbols.DIM, new IntArrayVector(rows,cols)).build();
  }
}

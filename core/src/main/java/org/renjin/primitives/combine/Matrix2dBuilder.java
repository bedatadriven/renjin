package org.renjin.primitives.combine;

import org.renjin.sexp.*;

/**
 * Builds a two-dimensional matrix using an underlying {@link Vector.Builder}
 */
public class Matrix2dBuilder {
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
    return builder.setAttribute(Symbols.DIM, new IntArrayVector(rows,cols))
            .build();
  }
}

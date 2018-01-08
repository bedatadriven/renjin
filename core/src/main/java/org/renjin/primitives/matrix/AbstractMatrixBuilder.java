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
package org.renjin.primitives.matrix;

import org.renjin.primitives.Indexes;
import org.renjin.sexp.*;

import java.util.Collection;


class AbstractMatrixBuilder<B extends Vector.Builder, V extends Vector>  {

  protected final B builder;
  private final int nrows;
  private final int ncols;
  
  private Vector rowNames = Null.INSTANCE;
  private Vector colNames = Null.INSTANCE;
  
  public AbstractMatrixBuilder(Vector.Type vectorType, int nrows, int ncols) {
    this.nrows = nrows;
    this.ncols = ncols;
    builder = (B) vectorType.newBuilderWithInitialSize(nrows * ncols);
    builder.setAttribute(Symbols.DIM, new IntArrayVector(nrows, ncols));
  }
 
  public void setRowNames(Vector names) {
    rowNames = names;
  }
  
  public void setRowNames(Collection<String> names) {
    rowNames = new StringArrayVector(names);
  }
  
  public void setColNames(Vector names) {
    colNames = names;
  }
  
  public void setColNames(Collection<String> names) {
    colNames = new StringArrayVector(names);
  }
  
  public int getRows() {
    return nrows;
  }
  
  public int getCols() {
    return ncols;
  }
  
  protected final int computeIndex(int row, int col) {
    return Indexes.matrixIndexToVectorIndex(row, col, nrows, ncols);
  }
  
  public V build() {
    if(rowNames != Null.INSTANCE || colNames != Null.INSTANCE) {
      builder.setAttribute(Symbols.DIMNAMES, new ListVector(rowNames, colNames));
    }
    return (V)builder.build();
  } 
}

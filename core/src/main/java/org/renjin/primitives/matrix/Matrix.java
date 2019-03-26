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
package org.renjin.primitives.matrix;

import org.renjin.primitives.Indexes;
import org.renjin.sexp.*;


/**
 * Wrapper class for an R {@link Vector} with two dimensions. 
 * Simplifies interaction with R matrices from java code.
 *
 */
public class Matrix {
  private final Vector vector;
  private final int nrows;
  private final int ncols;
  
  public Matrix(Vector vector) {
    Vector dim = (Vector) vector.getAttribute(Symbols.DIM);
    if(dim.length() != 2) {
      throw new IllegalArgumentException("vector is not a matrix");
    }
    this.vector = vector;
    this.nrows = dim.getElementAsInt(0);
    this.ncols = dim.getElementAsInt(1);
  }
  
  public Matrix(Vector vector, int ncols) {
    this.vector = vector;
    this.ncols = ncols;
    this.nrows = vector.length() / ncols;
  }

  public Vector getVector() {
    return vector;
  }

  public int getNumRows() {
    return nrows;
  }

  public int getNumCols() {
    return ncols;
  }

  public Vector getRowNames() {
    return getDimNames(0);
  }

  public Vector getColNames() {
    return getDimNames(1);
  }
  
  public String getRowName(int rowIndex) {
    Vector rowNames = getRowNames();
    if(rowNames == Null.INSTANCE) {
      return null;
    } else {
      return rowNames.getElementAsString(rowIndex);
    }
  }
  
  public String getColName(int colIndex) {
    Vector rowNames = getColNames();
    if(rowNames == Null.INSTANCE) {
      return null;
    } else {
      return rowNames.getElementAsString(colIndex);
    }
  }
  
  private Vector getDimNames(int dimensionIndex) {
    Vector dimNames = (Vector)vector.getAttribute(Symbols.DIMNAMES);
    if(dimNames.length() != 2) {
      return Null.INSTANCE; 
    } else {
      return (Vector)dimNames.getElementAsSEXP(dimensionIndex);
    }
  }
  

  private int computeIndex(int row, int col) {
    return Indexes.matrixIndexToVectorIndex(row, col, nrows, ncols);
  }
  
  public int getElementAsInt(int row, int col) {
    return vector.getElementAsInt(computeIndex(row, col));
  }
  
  public double getElementAsDouble(int row, int col) {
    return vector.getElementAsDouble(computeIndex(row, col));
  }
  
  public MatrixBuilder newBuilder(int rows, int cols) {
    if(vector instanceof IntVector) {
      return new IntMatrixBuilder(rows, cols);
    } else if(vector instanceof DoubleVector) {
      return new DoubleMatrixBuilder(rows, cols);
    } else {
      throw new UnsupportedOperationException("unimplemented type " + vector.getTypeName());
    }
  }

}

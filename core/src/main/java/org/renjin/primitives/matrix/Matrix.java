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
    Vector dimNames = (Vector)vector.getAttribute(Symbols.DIMNAMES);
    if(dimNames.length() != 2) {
      return Null.INSTANCE; 
    } else {
      return (Vector)dimNames.getElementAsSEXP(0);
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

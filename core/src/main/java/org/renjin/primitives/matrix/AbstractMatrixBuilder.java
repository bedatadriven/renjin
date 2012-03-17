package org.renjin.primitives.matrix;

import java.util.Collection;

import r.lang.Indexes;
import r.lang.IntVector;
import r.lang.ListVector;
import r.lang.Null;
import r.lang.StringVector;
import r.lang.Symbols;
import r.lang.Vector;

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
    builder.setAttribute(Symbols.DIM, new IntVector(nrows, ncols));
  }
 
  public void setRowNames(StringVector names) {
    rowNames = names;
  }
  
  public void setRowNames(Collection<String> names) {
    rowNames = new StringVector(names);
  }
  
  public void setColNames(StringVector names) {
    colNames = names;
  }
  
  public void setColNames(Collection<String> names) {
    colNames = new StringVector(names);
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

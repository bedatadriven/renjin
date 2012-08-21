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
  
  public void setColNames(StringVector names) {
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

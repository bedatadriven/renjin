package r.base.matrix;

import r.lang.IntVector;

public class IntMatrixBuilder extends AbstractMatrixBuilder<IntVector.Builder, IntVector>{
  
  public IntMatrixBuilder(int nrows, int ncols) {
    super(IntVector.VECTOR_TYPE, nrows, ncols);
  }
  
  public void set(int row, int col, int value) {
    builder.set(computeIndex(row, col), value);
  } 
}

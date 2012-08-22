package org.renjin.util;

import org.apache.commons.math.linear.AbstractRealMatrix;
import org.apache.commons.math.linear.BlockRealMatrix;
import org.apache.commons.math.linear.MatrixIndexException;
import org.apache.commons.math.linear.RealMatrix;
import org.renjin.primitives.Indexes;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.Null;
import org.renjin.sexp.Vector;


/**
 * Bridge between Renjin and the Commons Math APIs
 *
 */
public class CommonsMath {

  private CommonsMath() {}
  
  /**
   * Returns a view of a R matrix as a Commons Math {@code RealMatrix}.
   * 
   * @param vector an R vector
   * @return a view of the R matrix as a Commons Math {@code RealMatrix}
   * @throws IllegalArgumentException if the vector does not have a {@code dim} attribute, or if the vector
   * does not have exactly 2 dimensions
   */
  public static RealMatrix asRealMatrix(Vector vector) {
    return new MatrixAdapter(vector);
  }
  
  public static RealMatrix asRealMatrix(Vector vector, int numRows, int numCols) {
    if(numRows * numCols != vector.length()) {
      throw new IllegalArgumentException("numRows * numCols must equal the length of the vector");
    }
    return new MatrixAdapter(vector, numRows, numCols);
  }
  
  /**
   * Creates a copy of a Commons Math {@code RealMatrix} as an
   * {@code DoubleVector} with an appropriate {@code dim} attribute.
   * 
   * @param matrix
   * @return a {@code DoubleVector}
   */
  public static Vector asDoubleVector(RealMatrix matrix) {
    int nrows = matrix.getRowDimension();
    int ncols = matrix.getColumnDimension();
    
    DoubleArrayVector.Builder vector = DoubleArrayVector.Builder.withInitialSize(nrows * ncols);
    vector.setDim(nrows, ncols);
    
    int vector_i = 0;
    for(int i=0;i!=ncols;++i) {
      for(int j=0;j!=nrows;++j) {
        vector.set(vector_i++, matrix.getEntry(j, i));
      }
    }
    return vector.build();
  }
  
  
  private static class MatrixAdapter extends AbstractRealMatrix {
    private int nrows;
    private int ncols;
    private Vector vector;
    
    public MatrixAdapter(Vector vector) {
      this.vector = vector;
      
      Vector dim = vector.getAttributes().getDim();
      if(dim == Null.INSTANCE) {
        throw new IllegalArgumentException("the vector has no 'dim' attribute");
      }
      if(dim.length() != 2) {
        throw new IllegalArgumentException("the vector has is not a matrix; it has " + dim.length() + " dimension(s).");
      } 
      nrows = dim.getElementAsInt(0);
      ncols = dim.getElementAsInt(1);
    }
    
    public MatrixAdapter(Vector vector, int nrows, int ncols) {
      this.vector = vector;
      this.nrows = nrows;
      this.ncols = ncols;
    }
    
    
    @Override
    public RealMatrix createMatrix(int rowDimension, int columnDimension){
       return new BlockRealMatrix(rowDimension, columnDimension);
    }

    @Override
    public RealMatrix copy() {
      return(new MatrixAdapter(vector.newCopyBuilder().build()));
    }

    @Override
    public double getEntry(int row, int column) throws MatrixIndexException {
      return vector.getElementAsDouble(Indexes.matrixIndexToVectorIndex(row, column, nrows, ncols));
    }

    @Override
    public void setEntry(int row, int column, double value){
      throw new UnsupportedOperationException();
    }

    @Override
    public void addToEntry(int row, int column, double increment)
        throws MatrixIndexException {
      throw new UnsupportedOperationException();
    }

    @Override
    public void multiplyEntry(int row, int column, double factor)
        throws MatrixIndexException {
      throw new UnsupportedOperationException();
    }

    @Override
    public int getRowDimension() {
      return nrows;
    }

    @Override
    public int getColumnDimension() {
      return ncols;
    }
  }
}

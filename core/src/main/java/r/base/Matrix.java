package r.base;

import org.apache.commons.math.linear.RealMatrix;

import r.jvmi.annotations.Primitive;
import r.lang.AtomicVector;
import r.lang.DoubleVector;
import r.lang.Indexes;
import r.lang.IntVector;
import r.lang.ListVector;
import r.lang.Null;
import r.lang.SEXP;
import r.lang.Symbols;
import r.lang.Vector;
import r.lang.exception.EvalException;
import r.util.CommonsMath;

public class Matrix {



  @Primitive("t.default")
  public static Vector transpose(Vector x) {
    Vector dimensions = (Vector) x.getAttribute(Symbols.DIM);
    if(dimensions.length() == 0) {
      return (Vector)x.setAttribute(Symbols.DIM, new IntVector(1, x.length()));
      
    } else if(dimensions.length() == 2){
      Vector.Builder builder = x.newBuilderWithInitialSize(x.length());
      Vector result = builder.build();
      int nrows = dimensions.getElementAsInt(0);
      int ncols = dimensions.getElementAsInt(1);
      for (int i = 0; i < nrows; i++) {
        for (int j = 0; j < ncols; j++) {
          builder.setFrom(Indexes.matrixIndexToVectorIndex(j, i, ncols, nrows), x,
                          Indexes.matrixIndexToVectorIndex(i, j, nrows, ncols));
        }
      }
      if (!(x.getAttribute(Symbols.DIMNAMES) instanceof r.lang.Null)) {
        ListVector dimNames = (ListVector) x.getAttribute(Symbols.DIMNAMES);
        ListVector newDimNames = new ListVector(dimNames.get(1), dimNames.get(0));
        builder.setAttribute(Symbols.DIMNAMES, newDimNames);
      }
      builder.setAttribute(Symbols.DIM, new IntVector(ncols, nrows));
      result = builder.build();
      return (result);
      
    } else {
      throw new EvalException("argument is not a matrix");
    }
  }

  @Primitive("%*%")
  public static SEXP matrixproduct(AtomicVector x, AtomicVector y) {
    return new MatrixProduct(MatrixProduct.PROD, x, y)
      .matprod();
  }
  
  @Primitive("crossprod")
  public static SEXP crossprod(AtomicVector x, AtomicVector y) {
    return new MatrixProduct(MatrixProduct.CROSSPROD, x, y)
      .crossprod();
  }
  
  @Primitive("tcrossprod")
  public static SEXP tcrossprod(AtomicVector x, AtomicVector y) {
    return new MatrixProduct(MatrixProduct.TCROSSPROD, x, y)
      .tcrossprod();
  }
  
  
  public static DoubleVector rowSums(AtomicVector x, int numRows, int rowLength, boolean naRm) {
    
    double sums[] = new double[numRows];
    int sourceIndex = 0;
    for(int col=0;col < rowLength; col++) {
      for(int row=0;row<numRows;row++) {
        double value = x.getElementAsDouble(sourceIndex++);
        if(!naRm) {
          sums[row] += value;
        } else if(!Double.isNaN(value)) {
          sums[row] += value;
        }
      }
    }
    
    return new DoubleVector(sums);
  }
  
  public static DoubleVector rowMeans(AtomicVector x, int numRows, int rowLength, boolean naRm) {
    DoubleVector sums = rowSums(x, numRows,  rowLength, naRm);
    DoubleVector.Builder dvb = new DoubleVector.Builder();
    for (int i = 0; i < numRows; i++) {
      dvb.add(sums.get(i) / rowLength);
    }
    return (dvb.build());
  }
  
  public static DoubleVector colSums(AtomicVector x, int columnLength, int numColumns, boolean naRm) {
    
    double sums[] = new double[numColumns];
    for(int column=0;column < numColumns; column++) {
      int sourceIndex = columnLength*column;

      double sum = 0;
      for(int row=0;row < columnLength; ++row) {
        double cellValue = x.getElementAsDouble(sourceIndex++);
        if(Double.isNaN(cellValue)) {
          if(!naRm) {
            sum = DoubleVector.NA;
            break;
          }
        } else {
          sum += cellValue;
        }
      }
      sums[column] = sum;
    }
    
    return new DoubleVector(sums);
  }
  
  public static DoubleVector colMeans(AtomicVector x, int columnLength, int numColumns, boolean naRm) {
    DoubleVector sums = colSums(x, columnLength, numColumns, naRm);
    DoubleVector.Builder dvb = new DoubleVector.Builder();
    for (int i = 0; i < numColumns; i++) {
      dvb.add(sums.get(i) / columnLength);
    }
    return (dvb.build());
  }
}

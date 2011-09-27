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
    Vector.Builder builder = x.newBuilder(x.length());
    Vector result = builder.build();
    IntVector dimensions = (IntVector) x.getAttribute(Symbols.DIM);
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
  }

  @Primitive("%*%")
  public static SEXP matrixproduct(Vector x, Vector y) {
    
    AtomicVector xdims = (AtomicVector)x.getAttribute(Symbols.DIM);
    AtomicVector ydims = (AtomicVector)y.getAttribute(Symbols.DIM);
    int ldx = xdims.length();
    int ldy = ydims.length();

    int nrx=0, ncx=0, nry=0, ncy=0;

    if (ldx != 2 && ldy != 2) {         /* x and y non-matrices */
      nrx = 1;
      ncx = x.length();
      nry = y.length();
      ncy = 1;
    } else if (ldx != 2) {                /* x not a matrix */
      nry = ydims.getElementAsInt(0);
      ncy = ydims.getElementAsInt(1);
      if (x.length() == nry) {     /* x as row vector */
        nrx = 1;
        ncx = nry; /* == LENGTH(x) */
      } else if (nry == 1) {        /* x as col vector */
        nrx = x.length();
        ncx = 1;
      }
    } else if(ldy != 2) {
      nrx =  xdims.getElementAsInt(0);
      ncx =  xdims.getElementAsInt(1);
      if (y.length() == ncx) {     /* y as col vector */
        nry = ncx; /* == LENGTH(y) */
        ncy = 1;
      } else if (ncx == 1) {        /* y as row vector */
        nry = 1;
        ncy = y.length();
      }
    } else {
      nrx = xdims.getElementAsInt(0);
      ncx = xdims.getElementAsInt(1);
      nry = ydims.getElementAsInt(0);
      ncy = ydims.getElementAsInt(1);
    }
    
    if (ncx != nry) {
      throw new EvalException("non-conformable arguments");
    }
    
    RealMatrix prod = CommonsMath.asRealMatrix(x, nrx, ncx)
                          .multiply(CommonsMath.asRealMatrix(y, nry, ncy));
    
    SEXP xdimnames = x.getAttribute(Symbols.DIMNAMES);
    SEXP ydimnames = y.getAttribute(Symbols.DIMNAMES);
    
    if (xdimnames != Null.INSTANCE || xdimnames != Null.INSTANCE) {
      throw new EvalException("%*% for matricies with dimnnames not yet implemented");
//        SEXP dimnames, dimnamesnames, dnx=Null.INSTANCE, dny=R_NilValue;
//
//        /* allocate dimnames and dimnamesnames */
//
//        PROTECT(dimnames = allocVector(VECSXP, 2));
//        PROTECT(dimnamesnames = allocVector(STRSXP, 2));
//        if (xdims != R_NilValue) {
//            if (ldx == 2 || ncx == 1) {
//                SET_VECTOR_ELT(dimnames, 0, VECTOR_ELT(xdims, 0));
//                dnx = getAttrib(xdims, R_NamesSymbol);
//                if(!isNull(dnx))
//                    SET_STRING_ELT(dimnamesnames, 0, STRING_ELT(dnx, 0));
//            }
    }
    
    return CommonsMath.asDoubleVector(prod);
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
}

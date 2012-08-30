package org.renjin.primitives.matrix;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.primitives.Indexes;
import org.renjin.primitives.Warning;
import org.renjin.primitives.annotations.Current;
import org.renjin.primitives.annotations.Primitive;
import org.renjin.primitives.sequence.RepDoubleVector;
import org.renjin.primitives.vector.ComputingIntVector;
import org.renjin.primitives.vector.ConstantDoubleVector;
import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.sexp.*;


/**
 * Implementation of R primitive functions involving matrices.
 */
public class Matrices {

  private Matrices() {}

  @Primitive("t.default")
  public static Vector transpose(Vector x) {
    // Actually allocate the memory and perform transposition
    Vector dimensions = x.getAttributes().getDim();
    if(dimensions.length() == 0) {
      return (Vector)x.setAttributes(AttributeMap.dim(1, x.length()));

    } else if(dimensions.length() == 2) {
      int nrows = dimensions.getElementAsInt(0);
      int ncols = dimensions.getElementAsInt(1);

      if(x.length() > TransposingMatrix.LENGTH_THRESHOLD) {
        // Just wrap the matrix
        return new TransposingMatrix(x, AttributeMap.dim(ncols, nrows));

      } else {
        // actually allocate the memory
        Vector.Builder builder = x.newBuilderWithInitialSize(x.length());

        for (int i = 0; i < nrows; i++) {
          for (int j = 0; j < ncols; j++) {
            builder.setFrom(Indexes.matrixIndexToVectorIndex(j, i, ncols, nrows), x,
                    Indexes.matrixIndexToVectorIndex(i, j, nrows, ncols));
          }
        }
        if (!(x.getAttribute(Symbols.DIMNAMES) instanceof org.renjin.sexp.Null)) {
          ListVector dimNames = (ListVector) x.getAttribute(Symbols.DIMNAMES);
          ListVector newDimNames = new ListVector(dimNames.get(1), dimNames.get(0));
          builder.setAttribute(Symbols.DIMNAMES, newDimNames);
        }
        builder.setDim(ncols, nrows);
        return builder.build();
      }
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


  @Primitive
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

    return DoubleArrayVector.unsafe(sums);
  }

  @Primitive
  public static DoubleVector rowMeans(AtomicVector x,
                                      int numRows,
                                      int rowLength,
                                      boolean naRm) {
    if(!naRm && x instanceof DeferredComputation) {
      return new DeferredRowMeans(x, numRows, AttributeMap.EMPTY);
    }

    double sums[] = new double[numRows];
    int counts[] = new int[numRows];
    int sourceIndex = 0;
    for(int col=0;col < rowLength; col++) {
      for(int row=0;row<numRows;row++) {
        double value = x.getElementAsDouble(sourceIndex++);
        if(!naRm) {
          sums[row] += value;
          counts[row] ++;
        } else if(!Double.isNaN(value)) {
          sums[row] += value;
          counts[row] ++;
        }
      }
    }
    for(int row=0;row<numRows;row++) {
      sums[row] /= (double)counts[row];
    }
    return DoubleArrayVector.unsafe(sums);
  }

  @Primitive
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

    return new DoubleArrayVector(sums);
  }

  public static DoubleVector colMeans(AtomicVector x, int columnLength, int numColumns, boolean naRm) {
    DoubleVector sums = colSums(x, columnLength, numColumns, naRm);
    DoubleArrayVector.Builder dvb = new DoubleArrayVector.Builder();
    for (int i = 0; i < numColumns; i++) {
      dvb.add(sums.get(i) / columnLength);
    }
    return (dvb.build());
  }

  /**
   * Transpose an array by permuting its dimensions and optionally resizing it.
   * @param source the array to be transposed.
   * @param permutationVector the subscript permutation vector, which must be a permutation of the
   *      integers 1:n, where {@code n} is the number of dimensions of {@code source}.
   * @param resize flag indicating whether the vector should be resized as well as having its elements reordered
   * @return A transposed version of array a, with subscripts permuted as indicated by the array perm.
   * If resize is TRUE, the array is reshaped as well as having
   *  its elements permuted, the dimnames are also permuted; if resize = FALSE then the returned
   * object has the same dimensions as a, and the dimnames are dropped. In each case other attributes
   * are copied from a.
   */
  public static SEXP aperm(Vector source, AtomicVector permutationVector, boolean resize) {
    if(!resize) throw new UnsupportedOperationException("resize=TRUE not yet implemented");

    SEXP dimExp = source.getAttributes().getDim();
    EvalException.check(dimExp instanceof IntVector, "invalid first argument, must be an array");
    int permutation[] = toPermutationArray(permutationVector);

    if(isIdentityPermutation(permutation)) {
      /**
       * No actual change to the vector
       */
      return source;

    } else if(source instanceof DoubleVector && isMatrixTransposition(permutation) &&
            source.length() > TransposingMatrix.LENGTH_THRESHOLD) {

      /*
       * This is equivalent to a transposition, use
       * the faster transpose method
       */
      return transpose(source);

    } else {
      /*
       * Actually perform the permutation here
       */

      int dim[] = ((IntVector) dimExp).toIntArray();
      int permutedDims[] = Indexes.permute(dim, permutation);

      Vector.Builder newVector = source.newBuilderWithInitialSize(source.length());
      int index[] = new int[dim.length];
      for(int i=0;i!=newVector.length();++i) {
        Indexes.vectorIndexToArrayIndex(i, index, dim);
        index = Indexes.permute(index, permutation);
        int newIndex = Indexes.arrayIndexToVectorIndex(index, permutedDims);
        newVector.setFrom(newIndex, source, i);
      }

      newVector.setAttribute(Symbols.DIM, new IntArrayVector(permutedDims));

      for(PairList.Node node : source.getAttributes().nodes()) {
        if(node.getTag().equals(Symbols.DIM)) {

        } else if(node.getTag().equals(Symbols.DIMNAMES)) {
          newVector.setAttribute(node.getName(), Indexes.permute((Vector) node.getValue(), permutation));
        } else {
          newVector.setAttribute(node.getName(), node.getValue());
        }
      }
      return newVector.build();
    }
  }

  public static boolean isMatrixTransposition(int[] permutation) {
    return permutation.length == 2 && permutation[0] == 1 && permutation[1] == 0;
  }

  public static boolean isIdentityPermutation(int[] permutation) {
    for(int i=0;i!=permutation.length;++i) {
      if(permutation[i] != i) {
        return false;
      }
    }
    return true;
  }

  /**
   *
   * @param vector User-supplied 1-based permutation vector
   * @return  zero-based permutation int[]
   */
  public static int[] toPermutationArray(Vector vector) {
    int values[] = new int[vector.length()];
    for(int i=0;i!=values.length;++i) {
      values[i] = vector.getElementAsInt(i) - 1;
    }
    return values;
  }

  /**
   * Creates a matrix from the given set of values.
   *
   * @param data an optional data vector.
   * @param nrow the desired number of rows.
   * @param ncol the desired number of columns.
   * @param byRow If FALSE (the default) the matrix is filled by columns, otherwise the matrix is filled by rows.
   * @return
   */
  @Primitive
  public static Vector matrix(@Current Context context,
                              Vector data,
                              int nrow, int ncol,
                              boolean byRow,
                              Vector dimnames,
                              boolean nrowMissing, boolean ncolMissing) {

    int dataLength = data.length();

    if (nrowMissing && ncolMissing) {
      nrow = dataLength;
    } else if(nrowMissing) {
      nrow = (int)Math.ceil(dataLength / (double)ncol);
    } else if(ncolMissing) {
      ncol = (int)Math.ceil(dataLength / (double)nrow);
    }

    if(dataLength > 0) {
      if (dataLength > 1 && (nrow * ncol) % dataLength != 0) {
        if (((dataLength > nrow) && (dataLength / nrow) * nrow != dataLength) ||
                ((dataLength < nrow) && (nrow / dataLength) * dataLength != nrow)) {

          Warning.invokeWarning(context,
                  "data length [%d] is not a sub-multiple or multiple of the number of rows [%d]",
                  dataLength, nrow);

        } else if (((dataLength > ncol) && (dataLength / ncol) * ncol != dataLength) ||
                ((dataLength < ncol) && (ncol / dataLength) * dataLength != ncol)) {

          Warning.invokeWarning(context,
                  "data length [%d] is not a sub-multiple or multiple of the number of columns [%d]",
                  dataLength, ncol);
        }
      }  else if ((dataLength > 1) && (nrow * ncol == 0)){
        Warning.invokeWarning(context,
                "data length exceeds size of matrix");
      }
    }

    AttributeMap attributes = AttributeMap
            .builder()
            .setDim(nrow, ncol)
            .set(Symbols.DIMNAMES, dimnames)
            .build();

    /*
     * Avoid allocating huge arrays of data
     */
    int resultLength = (nrow * ncol);
    if(!byRow && resultLength > 500) {
      if(data instanceof DoubleVector) {
        if(data.length() == 1) {
          return new ConstantDoubleVector(data.getElementAsDouble(0), resultLength, attributes);
        } else {
          return new RepDoubleVector(data, resultLength, 1, attributes);
        }
      }
    }
    return allocMatrix(data, nrow, ncol, byRow, dimnames);
  }

  private static Vector allocMatrix(Vector data, int nrow, int ncol, boolean byRow, Vector dimnames) {
    Vector.Builder result = data.newBuilderWithInitialSize(nrow * ncol);
    int dataLength = data.length();
    int i = 0;

    if(dataLength > 0) {
      if (!byRow) {
        for (int col = 0; col < ncol; ++col) {
          for (int row = 0; row < nrow; ++row) {
            int sourceIndex = Indexes.matrixIndexToVectorIndex(row, col, nrow, ncol)
                    % dataLength;
            result.setFrom(i++, data, sourceIndex);
          }
        }
      } else {
        for (int row = 0; row < nrow; ++row) {
          for (int col = 0; col < ncol; ++col) {
            result.setFrom(row + (col * nrow), data, i % dataLength);
            i++;
          }
        }
      }
    }
    result.setDim(nrow, ncol);
    result.setAttribute(Symbols.DIMNAMES, dimnames);
    return result.build();
  }

  @Primitive
  public static IntVector row(IntVector dims){
    if(dims.length()!=2){
      throw new EvalException("a matrix-like object is required as argument to 'row/col'");
    }
    final int rows = dims.getElementAsInt(0);
    final int cols = dims.getElementAsInt(1);

    ComputingIntVector.Functor fn = new ComputingIntVector.Functor() {
      @Override
      public int apply(int index) {
        return (index % rows) + 1;
      }
    };

    return new ComputingIntVector(fn, rows * cols, AttributeMap.dim(rows, cols));
  }

  @Primitive
  public static IntVector col(IntVector dims) {
    if(dims.length()!=2){
      throw new EvalException("a matrix-like object is required as argument to 'row/col'");
    }
    final int rows = dims.getElementAsInt(0);
    final int cols = dims.getElementAsInt(1);

    ComputingIntVector.Functor fn = new ComputingIntVector.Functor() {
      @Override
      public int apply(int index) {
        return (index / rows) + 1;
      }
    };
    return new ComputingIntVector(fn, rows * cols, AttributeMap.dim(rows, cols));
  }
}

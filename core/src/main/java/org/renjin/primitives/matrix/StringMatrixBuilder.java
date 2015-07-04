package org.renjin.primitives.matrix;

import org.renjin.parser.NumericLiterals;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.StringVector;


public class StringMatrixBuilder extends AbstractMatrixBuilder<StringVector.Builder, StringVector>
 implements MatrixBuilder {

  public StringMatrixBuilder(int nrows, int ncols) {
    super(StringVector.VECTOR_TYPE, nrows, ncols);
  }

  @Override
  public void setValue(int row, int col, double value) {
    if(DoubleVector.isNA(value)) {
      setValue(row, col, StringVector.NA);
    } else {
      setValue(row, col, NumericLiterals.toString(value));
    }
  }

  @Override
  public void setValue(int row, int col, int value) {
    if(IntVector.isNA(value)) {
      setValue(row, col, StringVector.NA);
    } else {
      setValue(row, col, NumericLiterals.format(value));
    }
  }
  
  public void setValue(int row, int col, String value) {
    builder.set(computeIndex(row, col), value);
  }
 
}

package org.renjin.stats.internals.models;

import java.util.List;

import org.renjin.primitives.Indexes;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

public class ModelMatrix extends DoubleVector {

  /**
   * Attribute that associates columns of the matrix with thei
   * terms. It will look something like [0, 1, 2, 2, 2, 3, 3, 4], indicating
   * that the first column belongs to the intercept, the second to factor 1, 
   * the third and forth columns to factor 2, etc.
   */
  public static final Symbol ASSIGN = Symbol.get("assign");
  
  private int numRows = 0;
  private List<ModelMatrixColumn> columns;

  public ModelMatrix(int numRows, List<ModelMatrixColumn> columns,
      AttributeMap attributes) {
    super(attributes);
    this.numRows = numRows;
    this.columns = columns;
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new ModelMatrix(numRows, columns, attributes);
  }

  @Override
  public double getElementAsDouble(int index) {
    int col = Indexes.vectorIndexToCol(index, numRows, columns.size());
    int row = Indexes.vectorIndexToRow(index, numRows);
    return columns.get(col).getValue(row);
  }

  @Override
  public boolean isConstantAccessTime() {
    return true;
  }

  @Override
  public int length() {
    return numRows * columns.size();
  }
}

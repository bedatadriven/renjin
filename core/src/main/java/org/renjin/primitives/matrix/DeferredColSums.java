package org.renjin.primitives.matrix;

import org.renjin.primitives.vector.AttributeDecoratingVector;
import org.renjin.primitives.vector.MemoizedComputation;
import org.renjin.sexp.*;

public class DeferredColSums extends DoubleVector implements MemoizedComputation {

  private final AtomicVector vector;
  private int numColumns;
  private boolean naRm;
  private double[] sums = null;

  public DeferredColSums(AtomicVector vector, int numColumns, boolean naRm, AttributeMap attributes) {
    super(attributes);
    this.vector = vector;
    this.numColumns = numColumns;
    this.naRm = naRm;
  }

  @Override
  public Vector[] getOperands() {
    return new Vector[]{vector, new IntArrayVector(numColumns)};
  }

  @Override
  public String getComputationName() {
    return "colSums";
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new AttributeDecoratingVector(this, attributes);
  }

  @Override
  public double getElementAsDouble(int index) {
    if(this.sums == null) {
      System.err.println("EEK! colSums.computeMeans() called through getElementAsDouble()");
      computeMeans();
    }
    return sums[index];
  }

  @Override
  public boolean isConstantAccessTime() {
    return false;
  }

  @Override
  public int length() {
    return numColumns;
  }

  private void computeMeans() {
    double sums[] = new double[numColumns];
    int sourceIndex = 0;
    double sum = 0;

    int numRows = vector.length() / numColumns;
    int colIndex = 0;
    int rowIndex = 0;

    while (colIndex < numColumns) {
      double cellValue = vector.getElementAsDouble(sourceIndex++);
      if (!naRm || !Double.isNaN(cellValue)) {
        sum += cellValue;
      }
      rowIndex++;
      if (rowIndex == numRows) {
        rowIndex = 0;
        sums[colIndex] = sum;
        sum = 0;
        colIndex++;
      }
    }

    this.sums = sums;
  }

  @Override
  public boolean isCalculated() {
    return sums != null;
  }

  @Override
  public boolean isDeferred() {
    return !isCalculated();
  }

  @Override
  public Vector forceResult() {
    if(this.sums == null) {
      computeMeans();
    }
    return DoubleArrayVector.unsafe(this.sums);
  }

  @Override
  public void setResult(Vector result) {
    this.sums = ((DoubleArrayVector)result).toDoubleArrayUnsafe();
  }
}

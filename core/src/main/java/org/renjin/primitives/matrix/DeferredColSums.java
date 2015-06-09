package org.renjin.primitives.matrix;

import org.renjin.primitives.vector.AttributeDecoratingVector;
import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.primitives.vector.MemoizedComputation;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.IntArrayVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

public class DeferredColSums extends DoubleVector implements MemoizedComputation {

  private final AtomicVector vector;
  private int columnLength;
  private int numColumns;
  private boolean naRm;
  private double[] sums = null;

  public DeferredColSums(AtomicVector vector, int columnLength, int numColumns, boolean naRm, AttributeMap attributes) {
    super(attributes);
    this.vector = vector;
    this.columnLength = columnLength;
    this.numColumns = numColumns;
    this.naRm = naRm;
  }

  @Override
  public Vector[] getOperands() {
    return new Vector[] { vector, new IntArrayVector(numColumns) };
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
    // Thread.dumpStack();
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
    for(int column=0; column < numColumns; column++) {
      int sourceIndex = columnLength*column;
      double sum = 0;
      for(int row=0;row < columnLength; ++row) {
        double cellValue = vector.getElementAsDouble(sourceIndex++);
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
    this.sums = sums;
  }

  @Override
  public boolean isCalculated() {
    return sums != null;
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

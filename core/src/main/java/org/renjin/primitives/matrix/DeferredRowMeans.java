package org.renjin.primitives.matrix;

import org.renjin.primitives.vector.AttributeDecoratingVector;
import org.renjin.primitives.vector.MemoizedComputation;
import org.renjin.sexp.*;

public class DeferredRowMeans extends DoubleVector implements MemoizedComputation {

  private final AtomicVector vector;
  private int numRows;
  private int numCols;
  private double[] means;

  public DeferredRowMeans(AtomicVector vector, int numRows, AttributeMap attributes) {
    super(attributes);
    this.vector = vector;
    this.numRows = numRows;
    this.numCols = vector.length() / numRows;
  }

  @Override
  public Vector[] getOperands() {
    return new Vector[] { vector, new IntArrayVector(numRows) };
  }

  @Override
  public String getComputationName() {
    return "rowMeans";
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new AttributeDecoratingVector(this, attributes);
  }

  @Override
  public double getElementAsDouble(int index) {
    if(this.means == null) {
      computeMeans();
    }
    return means[index];
  }

  @Override
  public boolean isConstantAccessTime() {
    return false;
  }

  @Override
  public int length() {
    return numRows;
  }

  private void computeMeans() {
    System.err.println("EEK! rowMeans.calculate() called directly");

    double means[] = new double[numRows];
    int row = 0;
    for(int i=0;i!=vector.length();++i) {
      means[row] += vector.getElementAsDouble(i);
      row++;
      if(row == numRows) {
        row = 0;
      }
    }
    for(int i=0;i!=numRows;++i) {
      means[i] /= ((double)numCols);
    }
    this.means = means;
  }

  @Override
  public boolean isCalculated() {
    return means != null;
  }

  @Override
  public boolean isDeferred() {
    return !isCalculated();
  }

  @Override
  public Vector forceResult() {
    if(this.means == null) {
      computeMeans();
    }
    return DoubleArrayVector.unsafe(this.means);
  }

  @Override
  public void setResult(Vector result) {
    this.means = ((DoubleArrayVector)result).toDoubleArrayUnsafe();
  }
}

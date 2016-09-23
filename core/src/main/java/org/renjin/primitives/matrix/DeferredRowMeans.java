/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
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

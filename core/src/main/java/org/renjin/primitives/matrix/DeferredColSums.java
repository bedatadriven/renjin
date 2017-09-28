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
    if(naRm) {
      return "colSumsNaRm";
    } else {
      return "colSums";
    }
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new DeferredColSums(vector, numColumns, naRm, attributes);
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

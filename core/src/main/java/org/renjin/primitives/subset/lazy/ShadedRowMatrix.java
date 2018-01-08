/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
package org.renjin.primitives.subset.lazy;

import org.renjin.primitives.vector.MemoizedComputation;
import org.renjin.repackaged.guava.primitives.Ints;
import org.renjin.sexp.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShadedRowMatrix extends DoubleVector implements MemoizedComputation {

  private Vector base = null;
  private int columnLength = 1;
  private Map<Integer, Vector> rowMap = new HashMap<>();
  
  private double[] result;
  
  public ShadedRowMatrix(Vector source)  {
    super(source.getAttributes());
    this.base = source;
    int dimr[] = base.getAttributes().getDimArray();
    columnLength = dimr[0];
  }
  
  public ShadedRowMatrix withShadedRow(int row, Vector elements) {
    return cloneWithNewAttributes(this.getAttributes()).setShadedRow(row, elements);
  }
  
  public ShadedRowMatrix setShadedRow(int row, Vector elements) {
    rowMap.put(row, elements);
    return this;
  }

  @Override
  public boolean isConstantAccessTime() {
    return false;
  }

  @Override
  protected ShadedRowMatrix cloneWithNewAttributes(AttributeMap attributes) {    
    ShadedRowMatrix clone = new ShadedRowMatrix(this.base);
    clone.rowMap = new HashMap<>(rowMap);
    return clone;
  }

  @Override
  public double getElementAsDouble(int index) {
    int col = index / columnLength;
    int row = (index % columnLength);
    if (rowMap.containsKey(row)) {
      return rowMap.get(row).getElementAsDouble(col);
    } else {
      return base.getElementAsDouble(index);
    }
  }

  @Override
  public int length() {
    return base.length();
  }

  @Override
  public Vector[] getOperands() {
    List<Vector> ops = new ArrayList<Vector>();
    ops.add(base);
    ops.add(new IntArrayVector(Ints.toArray(rowMap.keySet())));
    ops.addAll(rowMap.values());
    return ops.toArray(new Vector[ops.size()]);
  }

  @Override
  public String getComputationName() {
    return "ShadedRowMatrix";
  }

  @Override
  public Vector forceResult() {
    double matrix[] = new double[length()];
    base.copyTo(matrix, 0, length());

    int rowLength = length() / columnLength;
    for (Map.Entry<Integer, Vector> entry : rowMap.entrySet()) {
      Vector vector = entry.getValue();
      int rowIndex = entry.getKey();
      int index = rowIndex;

      for (int i = 0; i < rowLength; ++i) {
        matrix[index] = vector.getElementAsDouble(i);
        index += columnLength;
      }
    }

    this.result = matrix;
    return DoubleArrayVector.unsafe(matrix, getAttributes());
  }

  @Override
  public void setResult(Vector result) {
    this.result = ((DoubleArrayVector) result).toDoubleArrayUnsafe();
  }

  @Override
  public boolean isCalculated() {
    return result != null;
  }

  @Override
  public boolean isDeferred() {
    return !isCalculated();
  }
}

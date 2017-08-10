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
package org.renjin.primitives.subset.lazy;

import org.renjin.repackaged.guava.primitives.Ints;
import org.renjin.primitives.vector.MemoizedComputation;
import org.renjin.sexp.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShadedRowMatrix extends DoubleVector implements MemoizedComputation {

  private DoubleVector base = null;
  private int columnLength = 1;
  private Map<Integer, DoubleVector> rowMap = new HashMap<Integer, DoubleVector>();
  
  private double[] result;
  
  public ShadedRowMatrix(DoubleVector source)  {
    super(source.getAttributes());
    this.base = source;
    SEXP dimr = base.getAttribute(Symbols.DIM);
    if (!(dimr instanceof IntArrayVector)) {
      throw new RuntimeException("non-integer dimensions? weird!");
    }
    columnLength = ((IntArrayVector)dimr).getElementAsInt(0);
  }
  
  public ShadedRowMatrix withShadedRow(int row, DoubleVector elements) {
    return cloneWithNewAttributes(this.getAttributes()).setShadedRow(row, elements);
  }
  
  public ShadedRowMatrix setShadedRow(int row, DoubleVector elements) {
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
    clone.rowMap = new HashMap<Integer, DoubleVector>(rowMap);
    return clone;
  }

  @Override
  public double getElementAsDouble(int index) {
    int col = index / columnLength;
    int row = (index % columnLength)+1;
    if (rowMap.containsKey(row)) {
      return rowMap.get(row).get(col);
    } else {
      return base.get(index);
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

    int rowLength = length() / columnLength;
    double matrix[] = base.toDoubleArray();
    for (Map.Entry<Integer, DoubleVector> entry : rowMap.entrySet()) {
      DoubleVector vector = entry.getValue();
      int rowIndex = entry.getKey()-1;
      int index = rowIndex;

      for (int i = 0; i < rowLength; ++i) {
        matrix[index] = vector.getElementAsDouble(i);
        index += columnLength;
      }
    }
    
    this.result = matrix;
    return DoubleArrayVector.unsafe(matrix);
  }

  @Override
  public void setResult(Vector result) {
    this.result = ((DoubleArrayVector) result).toDoubleArrayUnsafe();
  }

  @Override
  public boolean isCalculated() {
    return result != null;
  }
}

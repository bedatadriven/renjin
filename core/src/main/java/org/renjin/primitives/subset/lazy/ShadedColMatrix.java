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

public class ShadedColMatrix extends DoubleVector implements MemoizedComputation {

  private DoubleVector base = null;
  private int colheight = 1;
  private Map<Integer, DoubleVector> columnMap = new HashMap<Integer, DoubleVector>();
  
  private double result[];
  
  public ShadedColMatrix(DoubleVector source)  {
    super(source.getAttributes());
    this.base = source;
    SEXP dimr = base.getAttribute(Symbols.DIM);
    if (!(dimr instanceof IntArrayVector)) {
      throw new RuntimeException("non-integer dimensions? weird!");
    }
    colheight = ((IntArrayVector)dimr).getElementAsInt(0);
  }
  
  public ShadedColMatrix withShadedCol(int col, DoubleVector elements) {
    return cloneWithNewAttributes(this.getAttributes()).setShadedCol(col, elements);
  }
  
  public ShadedColMatrix setShadedCol(int col, DoubleVector elements) {
    columnMap.put(col, elements);
    return this;
  }

  @Override
  public boolean isConstantAccessTime() {
    return false;
  }

  @Override
  protected ShadedColMatrix cloneWithNewAttributes(AttributeMap attributes) {    
    ShadedColMatrix clone = new ShadedColMatrix(this.base);
    clone.columnMap = new HashMap<Integer, DoubleVector>(columnMap);
    return clone;
  }

  @Override
  public double getElementAsDouble(int index) {
    int col = index / colheight + 1;
    int row = (index % colheight);
    if (columnMap.containsKey(col)) {
      return columnMap.get(col).get(row);
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
    ops.add(new IntArrayVector(Ints.toArray(columnMap.keySet())));
    ops.addAll(columnMap.values());
    return ops.toArray(new Vector[ops.size()]);
  }

  @Override
  public String getComputationName() {
    return "ShadedColMatrix";
  }

  @Override
  public Vector forceResult() {
    int numColumns = length() / colheight;
    // allocate a copy of the base array
    double [] matrix = base.toDoubleArray();
    int index = 0;
    for(int col=0;col<numColumns;++col) {
      DoubleVector column = columnMap.get(col+1);
      if(column == null) {
        // skip this column, use the base values
        index += colheight;
      
      } else {
        // read this column from the operand
        for(int row=0;row<colheight;++row) {
          matrix[index++] = column.getElementAsDouble(row);
        }
      }
    }
    this.result = matrix;
    
    return DoubleArrayVector.unsafe(this.result, getAttributes());
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

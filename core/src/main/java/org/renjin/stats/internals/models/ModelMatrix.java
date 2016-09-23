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
package org.renjin.stats.internals.models;

import org.renjin.primitives.Indexes;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import java.util.List;

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

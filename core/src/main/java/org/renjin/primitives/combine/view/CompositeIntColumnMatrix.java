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
package org.renjin.primitives.combine.view;

import org.renjin.eval.Context;
import org.renjin.primitives.subset.ArraySubsettable;
import org.renjin.primitives.subset.IndexIterator;
import org.renjin.primitives.subset.MissingSubscript;
import org.renjin.primitives.subset.Subscript;
import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.sexp.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A matrix formed from combining two or more columns.
 */
public class CompositeIntColumnMatrix extends IntVector implements DeferredComputation , ArraySubsettable{

  private int columnLength;
  private int length;

  private final Vector[] columns;

  public CompositeIntColumnMatrix(Vector[] columns, AttributeMap attributeMap) {
    super(attributeMap);
    this.columns = columns;
    this.columnLength = columns[0].length();
    this.length = columns.length * columnLength;
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  public int getElementAsInt(int index) {
    int columnIndex = index / columnLength;
    int rowIndex = index % columnLength;
    return columns[columnIndex].getElementAsInt(rowIndex);
  }

  @Override
  public boolean isConstantAccessTime() {
    return true;
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new CompositeIntColumnMatrix(columns, attributes);
  }

  @Override
  public boolean isDeferred() {
    return true;
  }

  @Override
  public Vector[] getOperands() {
    return columns;
  }

  @Override
  public String getComputationName() {
    return "cols";
  }

  @Override
  public Vector subscript(Context context, int[] sourceDim, Subscript[] subscripts) {
    if (sourceDim.length == 2 &&
        sourceDim[1] == columns.length &&
        subscripts[0] instanceof MissingSubscript) {

      // Select a subset of these columns
      IndexIterator columnIt = subscripts[1].computeIndexes();
      List<Vector> selectedColumns = new ArrayList<>();
      int columnIndex;
      while( (columnIndex = columnIt.next()) != IndexIterator.EOF) {
        selectedColumns.add(columns[columnIndex]);
      }

      if(selectedColumns.size() == 0) {
        return IntArrayVector.EMPTY;

      } else if(selectedColumns.size() == 1) {
        return selectedColumns.get(0);

      } else {
        Vector[] selectedColumnsArray =
            selectedColumns.toArray(new Vector[selectedColumns.size()]);

        return new CompositeIntColumnMatrix(selectedColumnsArray, AttributeMap.EMPTY);
      }
    }

    // No specialization possible, proceed with a copy.
    return null;
  }

  @Override
  public void copyTo(double[] array, int offset, int length) {
    for (int i = 0; i < columns.length; i++) {
      columns[i].copyTo(array, offset + (i * columnLength), columnLength);
    }
  }
}

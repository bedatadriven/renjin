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
package org.renjin.primitives.matrix;

import org.renjin.primitives.vector.DeferredFunction;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.DoubleVector;

public class DeferredRowMeans implements DeferredFunction {

  public static final DeferredRowMeans INSTANCE = new DeferredRowMeans();

  private DeferredRowMeans() {
  }

  @Override
  public String getComputationName() {
    return "rowMeans";
  }


  @Override
  public int computeLength(AtomicVector[] operands) {
    return operands[1].asInt();
  }

  @Override
  public DoubleVector compute(AtomicVector[] operands) {
    AtomicVector vector = operands[0];
    int numRows = operands[1].asInt();
    int numCols = vector.length() / numRows;

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
    return DoubleArrayVector.unsafe(means);
  }

}

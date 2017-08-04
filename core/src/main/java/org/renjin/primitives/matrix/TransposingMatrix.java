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


import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.sexp.*;

public class TransposingMatrix extends DoubleVector implements DeferredComputation {

  public static final int LENGTH_THRESHOLD = 0;

  private final Vector source;
  private int[] sourceDim;
  private int sourceRowCount;
  private int sourceColCount;

  public TransposingMatrix(Vector source, AttributeMap attributes) {
    super(attributes);
    this.source = source;
    this.sourceDim = ((IntVector)source.getAttribute(Symbols.DIM)).toIntArray();
    this.sourceRowCount = sourceDim[0];
    this.sourceColCount = sourceDim[1];
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new TransposingMatrix(source, attributes);
  }

  @Override
  public double getElementAsDouble(int vectorIndex) {
    int row = vectorIndex % sourceColCount;
    vectorIndex = (vectorIndex - row) / sourceColCount;
    int col = vectorIndex % sourceRowCount;

    return source.getElementAsDouble(col + (row * sourceRowCount));
  }

  @Override
  public boolean isConstantAccessTime() {
    return true;
  }

  @Override
  public boolean isDeferred() {
    return true;
  }

  @Override
  public int length() {
    return source.length();
  }

  @Override
  public Vector[] getOperands() {
    return new Vector[] { source, new IntArrayVector(sourceRowCount) };
  }

  @Override
  public String getComputationName() {
    return "t";
  }

  @Override
  public int getComputationDepth() {
    return source.getComputationDepth() + 1;
  }
}

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
package org.renjin.primitives.vector;

import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

public abstract class MemoizedDoubleVector extends DoubleVector implements MemoizedComputation {

  private final Vector[] operands;
  private final int length;
  private Vector result;


  public MemoizedDoubleVector(Vector[] operands, int length, AttributeMap attributes) {
    super(attributes);
    this.operands = operands;
    this.length = length;
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new AttributeDecoratingVector(this, attributes);
  }

  @Override
  public double getElementAsDouble(int index) {
    if(result == null) {
      forceResult();
    }
    return result.getElementAsDouble(index);
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  public void setResult(Vector result) {
    this.result = result;
  }

  @Override
  public boolean isCalculated() {
    return result != null;
  }

  @Override
  public final boolean isDeferred() {
    return !isCalculated();
  }

  @Override
  public Vector[] getOperands() {
    return operands;
  }
}

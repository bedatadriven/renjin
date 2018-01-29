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
package org.renjin.primitives.vector;

import org.renjin.sexp.*;

public final class MemoizedDoubleVector extends DoubleVector implements DeferredComputation {

  private final DeferredFunction function;
  private final AtomicVector[] operands;
  private final int length;

  private Vector result;


  public MemoizedDoubleVector(AtomicVector[] operands, DeferredFunction function, AttributeMap attributes) {
    super(attributes);
    this.operands = operands;
    this.length = function.computeLength(operands);
    this.function = function;
  }

  public MemoizedDoubleVector(AtomicVector[] operands, DeferredFunction function) {
    this(operands, function, AttributeMap.EMPTY);
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new MemoizedDoubleVector(operands, function, attributes);
  }

  @Override
  public final double getElementAsDouble(int index) {
    if(result == null) {
      forceResult();
    }
    return result.getElementAsDouble(index);
  }

  private Vector forceResult() {
    if(result == null) {
      result =  function.compute(operands);
    }
    return result;
  }

  @Override
  public final int length() {
    return length;
  }

  public final void setResult(Vector result) {
    this.result = result;
  }

  public final boolean isCalculated() {
    return result != null;
  }

  @Override
  public final boolean isDeferred() {
    return !isCalculated();
  }

  @Override
  public final Vector[] getOperands() {
    return operands;
  }

  @Override
  public String getComputationName() {
    return function.getComputationName();
  }

  @Override
  public final boolean isConstantAccessTime() {
    return isCalculated();
  }

  @Override
  public String toString() {
    if(isCalculated()) {
      return DoubleVector.toString(this);
    } else {
      return "{" + getComputationName() + "}";
    }
  }
}

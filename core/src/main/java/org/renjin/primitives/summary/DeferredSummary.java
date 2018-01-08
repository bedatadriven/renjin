/**
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
package org.renjin.primitives.summary;

import org.renjin.primitives.vector.MemoizedComputation;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.Vector;


public abstract class DeferredSummary extends DoubleVector implements MemoizedComputation {
  protected final Vector vector;
  private double result;
  private boolean calculated = false;

  public DeferredSummary(Vector vector, AttributeMap attributes) {
    super(attributes);
    this.vector = vector;
  }

  @Override
  public final Vector[] getOperands() {
    return new Vector[]  { vector };
  }

  @Override
  public final double getElementAsDouble(int index) {
    if(index != 0) {
      throw new IllegalArgumentException("index: " + index);
    }
    if(!calculated) {
      result = calculate();
      calculated = true;
    }
    return result;
  }

  protected abstract double calculate();

  @Override
  public final int length() {
    return 1;
  }

  @Override
  public final boolean isConstantAccessTime() {
    return false;
  }

  @Override
  public final boolean isCalculated() {
    return calculated;
  }

  @Override
  public boolean isDeferred() {
    return !isCalculated();
  }

  @Override
  public final Vector forceResult() {
    if(!calculated) {
      result = calculate();
      calculated = true;
    }
    return new DoubleArrayVector(result, getAttributes());
  }

  @Override
  public final void setResult(Vector result) {
    this.result = result.getElementAsDouble(0);
    this.calculated = true;
  }

  @Override
  public String toString() {
    if(calculated) {
      return Double.toString(result);
    } else {
      return "<deferred " + getComputationName() + ">";
    }
  }
}

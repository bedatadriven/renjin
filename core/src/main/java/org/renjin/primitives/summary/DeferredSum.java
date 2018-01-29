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
package org.renjin.primitives.summary;

import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.primitives.vector.DeferredFunction;
import org.renjin.sexp.*;

public class DeferredSum implements DeferredFunction {

  public static final DeferredSum INSTANCE = new DeferredSum();

  private DeferredSum() {
  }

  @Override
  public String getComputationName() {
    return "sum";
  }

  @Override
  public DoubleVector compute(AtomicVector[] operands) {
    AtomicVector vector = operands[0];
    double sum = 0;
    for(int i=0;i!=vector.length();++i) {
      sum += vector.getElementAsDouble(i);
    }
    return new DoubleArrayVector(sum);
  }

  @Override
  public int computeLength(AtomicVector[] operands) {
    return 1;
  }
}

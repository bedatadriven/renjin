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
package org.renjin.primitives.subset;

import org.renjin.primitives.vector.MemoizedComputation;
import org.renjin.sexp.*;

/**
 * Deferred double vector created by the expression {@code x[i] <- y}. 
 */
class MaskedDoubleReplacement extends DoubleVector implements MemoizedComputation {

  private AtomicVector source;
  private LogicalVector mask;
  private AtomicVector replacement;

  private DoubleVector result = null;
  
  public MaskedDoubleReplacement(AttributeMap attributes, AtomicVector source, LogicalVector mask, AtomicVector replacement) {
    super(attributes);
    this.source = source;
    this.mask = mask;
    this.replacement = replacement;
  }


  @Override
  public Vector[] getOperands() {
    return new Vector[] { source, mask, replacement };
  }

  @Override
  public String getComputationName() {
    return "[[<-";
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new MaskedDoubleReplacement(attributes, source, mask, replacement);
  }

  @Override
  public double getElementAsDouble(int index) {
    if(result == null) {
      throw new IllegalStateException("Not computed");
    }
    return result.getElementAsDouble(index);
  }

  @Override
  public boolean isConstantAccessTime() {
    return false;
  }

  @Override
  public int length() {
    return source.length();
  }

  @Override
  public Vector forceResult() {
    int resultLength = Math.max(mask.length(), source.length());

    DoubleArrayVector.Builder result = new DoubleArrayVector.Builder(resultLength, resultLength);
    result.copyAttributesFrom(this);

    int maskLength = mask.length();
    int replacementLength = replacement.length();
    int replacementIndex = 0;
    for (int i = 0; i < resultLength; i++) {
      int maskValue = mask.getElementAsRawLogical(i % maskLength);
      if(maskValue == 1) {
        result.set(i, replacement.getElementAsDouble((replacementIndex++) % replacementLength)); 
      } else if(i < source.length()) {
        result.set(i, source.getElementAsDouble(i));
      }
    }
    this.result = result.build();
    return this.result;
  }

  @Override
  public void setResult(Vector result) {
    this.result = (DoubleVector) result;
  }

  @Override
  public boolean isCalculated() {
    return result != null;
  }

}

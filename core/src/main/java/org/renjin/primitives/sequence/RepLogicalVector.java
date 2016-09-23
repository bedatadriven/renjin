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
package org.renjin.primitives.sequence;

import org.renjin.eval.EvalException;
import org.renjin.sexp.*;

public class RepLogicalVector extends LogicalVector {

  public static final int LENGTH_THRESHOLD = 100;

  private final Vector source;
  private int length;
  private int each;

  public RepLogicalVector(Vector source, int length, int each, AttributeMap attributes) {
    super(attributes);
    this.source = source;
    this.length = length;
    this.each = each;
    if(this.length <= 0) {
      throw new IllegalArgumentException("length: " + length);
    }
  }
  
  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new RepLogicalVector(source, length, each, attributes);
  }

  @Override
  public boolean isConstantAccessTime() {
    return true;
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  public int getElementAsRawLogical(int index) {
    return source.getElementAsInt((index / each) % source.length());
  }

  public static Builder newConstantBuilder(Logical value, int length) {
    return new Builder(value, length);
  }

  public static class Builder extends AbstractAtomicBuilder {
    private LogicalVector vector;
    private int length = 1;
    private int each = 1;

    public Builder(Logical value, int length) {
      switch (value) {
        case TRUE:
          this.vector = LogicalVector.TRUE;
          break;
        case FALSE:
          this.vector = LogicalVector.FALSE;
          break;
        default:
          this.vector = LogicalVector.NA_VECTOR;
          break;
      }
      this.length = length;
    }

    @Override
    public int length() {
      return length;
    }

    @Override
    public LogicalVector build() {
      return new RepLogicalVector(vector, length, each, buildAttributes());
    }

    @Override
    public Builder setNA(int index) {
      throw new EvalException("cannot set na on constant builder");
    }

    @Override
    public Builder setFrom(int destinationIndex,
                           Vector source, int sourceIndex) {
      throw new EvalException("cannot set from on constant builder");
    }

    @Override
    public Builder add(Number value) {
      throw new EvalException("cannot add something to constant builder");
    }
  }
}

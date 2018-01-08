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
package org.renjin.primitives.vector;

import org.renjin.sexp.*;

/**
 * Lazily-evaluated is.na()
 */
public class IsNaVector extends LogicalVector implements DeferredComputation {
  private final Vector vector;

  public IsNaVector(Vector vector) {
    super(buildAttributes(vector));
    this.vector = vector;
  }

  private static AttributeMap buildAttributes(Vector vector) {
    AttributeMap sourceAttributes = vector.getAttributes();
    return AttributeMap.builder()
        .addIfNotNull(sourceAttributes, Symbols.DIM)
        .addIfNotNull(sourceAttributes, Symbols.NAMES)
        .addIfNotNull(sourceAttributes, Symbols.DIMNAMES)
        .validateAndBuildFor(vector);
  }

  private IsNaVector(AttributeMap attributes, Vector vector) {
    super(attributes);
    this.vector = vector;
  }


  @Override
  public int length() {
    return vector.length();
  }

  @Override
  public int getElementAsRawLogical(int index) {
    return vector.isElementNaN(index) ? 1 : 0;
  }

  @Override
  public boolean isConstantAccessTime() {
    return vector.isConstantAccessTime();
  }

  @Override
  public boolean isDeferred() {
    return true;
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new IsNaVector(attributes, vector);
  }

  @Override
  public Vector[] getOperands() {
    return new Vector[] { vector };
  }

  @Override
  public String getComputationName() {
    return "is.na";
  }
}

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
import org.renjin.sexp.Vector;

public class ConvertingDoubleVector extends DoubleVector implements DeferredComputation {

  private final Vector operand;

  public ConvertingDoubleVector(Vector operand, AttributeMap attributes) {
    super(attributes);
    this.operand = operand;
  }

  public ConvertingDoubleVector(Vector operand) {
    this(operand, AttributeMap.EMPTY);
  }

  @Override
  public int length() {
    return operand.length();
  }

  @Override
  protected DoubleVector cloneWithNewAttributes(AttributeMap attributes) {
    return new ConvertingDoubleVector(operand, attributes);
  }

  @Override
  public Vector[] getOperands() {
    return new Vector[] {operand};
  }

  @Override
  public String getComputationName() {
    return "as.double";
  }

  @Override
  public double getElementAsDouble(int index) {
    return operand.getElementAsDouble(index);
  }
  
  @Override
  public boolean isConstantAccessTime() {
    return operand.isConstantAccessTime();
  }

  @Override
  public boolean isDeferred() {
    return true;
  }

  public static double compute(double x) {
    return x;
  }

  @Override
  public double[] toDoubleArray() {
    double[] array = new double[length()];
    operand.copyTo(array, 0, length());
    return array;
  }
}

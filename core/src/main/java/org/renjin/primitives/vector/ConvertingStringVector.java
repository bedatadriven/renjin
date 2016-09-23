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
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Vector;

/**
 * A vector which converts its operand to Strings on the fly, so
 * that extra memory to does not need to be allocated.
 */
public class ConvertingStringVector extends StringVector implements DeferredComputation {

  private final Vector operand;

  public ConvertingStringVector(Vector operand, AttributeMap attributes) {
    super(attributes);
    this.operand = operand;
  }

  public ConvertingStringVector(Vector operand) {
    this(operand, AttributeMap.EMPTY);
  }

  public Vector getOperand() {
    return operand;
  }

  @Override
  public String getElementAsString(int index) {
    if(operand.isElementNA(index)) {
      return NA;
    }
    return operand.getElementAsString(index);
  }

  @Override
  public int getElementAsInt(int index) {
    return operand.getElementAsInt(index); 
  }

  @Override
  public int length() {
    return operand.length();
  }

  @Override
  protected StringVector cloneWithNewAttributes(AttributeMap attributes) {
    return new ConvertingStringVector(operand, attributes);
  }

  @Override
  public Vector[] getOperands() {
    return new Vector[] {operand};
  }

  @Override
  public String getComputationName() {
    return "as.character";
  }

  @Override
  public boolean isDeferred() {
    return true;
  }

  @Override
  public boolean isConstantAccessTime() {
    return true;
  }
  
}

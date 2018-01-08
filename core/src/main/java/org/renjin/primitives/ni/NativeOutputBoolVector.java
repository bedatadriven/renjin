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
package org.renjin.primitives.ni;

import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.LogicalVector;
import org.renjin.sexp.SEXP;


public class NativeOutputBoolVector extends LogicalVector implements NativeOutputVector {

  private final DeferredNativeCall call;
  private final int outputIndex;
  private final int length;

  private boolean[] array;

  public NativeOutputBoolVector(DeferredNativeCall call, int outputIndex, int length, AttributeMap attributes) {
    super(attributes);
    this.call = call;
    this.outputIndex = outputIndex;
    this.length = length;
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new NativeOutputBoolVector(call, outputIndex, length, attributes);
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  public int getElementAsRawLogical(int index) {
    if(array == null) {
      array = (boolean[]) call.output(outputIndex);
    }
    return array[index] ? 1: 0;
  }

  @Override
  public boolean isConstantAccessTime() {
    return call.isEvaluated();
  }

  @Override
  public boolean isDeferred() {
    return !call.isEvaluated();
  }

  @Override
  public DeferredNativeCall getCall() {
    return call;
  }

  @Override
  public int getOutputIndex() {
    return outputIndex;
  }
}

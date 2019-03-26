/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
package org.renjin.pipeliner.node;

import org.renjin.primitives.ni.DeferredNativeCall;
import org.renjin.repackaged.asm.Type;
import org.renjin.sexp.Vector;

/**
 * A call to a compiled Fortran or C subroutine that can have multiple outputs.
 */
public class CallNode extends DeferredNode implements Runnable {

  private DeferredNativeCall call;

  public CallNode(DeferredNativeCall call) {
    super();
    this.call = call;
  }

  @Override
  public String getDebugLabel() {
    return call.getDebugName();
  }

  @Override
  public NodeShape getShape() {
    return NodeShape.ELLIPSE;
  }

  @Override
  public Type getResultVectorType() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void run() {

    Vector inputs[] = new Vector[getOperands().size()];
    for (int i = 0; i < inputs.length; i++) {
      inputs[i] = getOperand(i).getVector();
    }

    call.evaluate(inputs);
  }
}

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
package org.renjin.compiler.pipeline.node;

import org.renjin.primitives.ni.NativeOutputVector;
import org.renjin.repackaged.asm.Type;
import org.renjin.sexp.Vector;

/**
 * A Vector that is the output of 
 */
public class OutputNode extends DeferredNode {
  
  private NativeOutputVector vector;

  public OutputNode(NativeOutputVector vector) {
    super();
    this.vector = vector;
  }

  @Override
  public String getDebugLabel() {
    return vector.getCall().getOutputName(vector.getOutputIndex());
  }

  @Override
  public NodeShape getShape() {
    return NodeShape.BOX;
  }

  @Override
  public Type getResultVectorType() {
    return Type.getType(vector.getClass());
  }

  @Override
  public Vector getVector() {
    return vector;
  }
}

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
package org.renjin.compiler.builtins;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.primitives.subset.MatrixSelection;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;

import java.util.List;

/**
 * Specialization of a subset operation known to be a matrix subset.
 */
public class MatrixSubset implements Specialization {
  private final ValueBounds source;
  private final List<ValueBounds> subscripts;
  private ValueBounds result;

  private boolean drop = true;
  
  public MatrixSubset(ValueBounds source, List<ValueBounds> subscripts) {
    this.source = source;
    this.subscripts = subscripts;
    this.result = MatrixSelection.computeResultBounds(source, subscripts, true);
  }


  /**
   * Try to infer result length, dimensions, etc.
   */
  public Specialization tryFurtherSpecialize() {
    return this;
  }

  @Override
  public Type getType() {
    return result.storageType();
  }

  public ValueBounds getResultBounds() {
    return result;
  }

  @Override
  public void load(EmitContext emitContext, InstructionAdapter mv, List<IRArgument> arguments) {
    throw new UnsupportedOperationException();
  }
}

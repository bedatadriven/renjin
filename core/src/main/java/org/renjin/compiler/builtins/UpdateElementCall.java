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

import org.renjin.compiler.builtins.FailedToSpecializeException;
import org.renjin.compiler.builtins.Specialization;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;

import java.util.List;

/**
 * Updates a single element in an atomic vector with a new scalar value.
 */
public class UpdateElementCall implements Specialization {
  
  private ValueBounds inputVector;
  private ValueBounds subscript;
  private ValueBounds replacement;

  public UpdateElementCall(ValueBounds inputVector, ValueBounds subscript, ValueBounds replacement) {
    this.inputVector = inputVector;
    this.subscript = subscript;
    this.replacement = replacement;
  }

  @Override
  public Type getType() {
    return inputVector.storageType();
  }

  public ValueBounds getResultBounds() {
    return inputVector.withVaryingValues();
  }

  @Override
  public void load(EmitContext emitContext, InstructionAdapter mv, List<IRArgument> arguments) {
    throw new FailedToSpecializeException("TODO");
  }
}

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
package org.renjin.compiler.builtins;


import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;

import java.util.List;

public class UnspecializedCall implements Specialization {

  public static final UnspecializedCall INSTANCE = new UnspecializedCall(false);

  public static final UnspecializedCall PURE = new UnspecializedCall(true);

  private final boolean pure;

  private UnspecializedCall(boolean pure) {
    this.pure = pure;
  }

  public ValueBounds getResultBounds() {
    return ValueBounds.UNBOUNDED;
  }

  @Override
  public boolean isPure() {
    return false;
  }

  @Override
  public CompiledSexp getCompiledExpr(EmitContext emitContext, List<IRArgument> arguments) {
    throw new FailedToSpecializeException();
  }

}

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

import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.sexp.Null;

import java.util.List;

/**
 * Verifies that a builtin has no S3 overloads before delegating to a
 * specializer for the default primitive method.
 */
public class GenericBuiltinGuard implements Specializer {
  
  private final BuiltinSpecializer specializer;

  public GenericBuiltinGuard(BuiltinSpecializer specializer) {
    this.specializer = specializer;
  }

  @Override
  public Specialization trySpecialize(RuntimeState runtimeState, List<ValueBounds> argumentTypes) {
    ValueBounds object = argumentTypes.get(0);
    if(object.isClassAttributeConstant()) {
      if(object.getConstantClassAttribute() == Null.INSTANCE) {
        // The argument has no class attribute, so we can safely
        // specialize the (builtin) default function.
        return specializer.trySpecialize(runtimeState, argumentTypes);
      }
    }
    // Can't make any assumptions. 
    return UnspecializedCall.INSTANCE;
  }
}

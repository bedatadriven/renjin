/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${$file.lastModified.year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */

package org.renjin.compiler.codegen.var;

import org.renjin.compiler.TypeSolver;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.expressions.LValue;

import java.util.HashMap;
import java.util.Map;

public class VariableMap {
  private LocalVarAllocator localVars;
  private Map<LValue, VariableStrategy> map = new HashMap<>();

  public VariableMap(LocalVarAllocator localVars, TypeSolver typeSolver) {
    this.localVars = localVars;
    for (Map.Entry<LValue, ValueBounds> entry : typeSolver.getVariables().entrySet()) {
      map.put(entry.getKey(), findBestStrategy(entry.getValue()));
    }
  }

  private VariableStrategy findBestStrategy(ValueBounds bounds) {

    if(bounds.isConstant()) {
      return new ConstantVar(bounds.getConstantValue());
    }

    if(TypeSet.size(bounds.getTypeSet()) == 1 && bounds.isScalar()) {
      return new ScalarVar(localVars, bounds);
    }

    if(TypeSet.size(bounds.getTypeSet()) == 1 && TypeSet.isDefinitelyAtomic(bounds.getTypeSet())) {
      return new ArrayVar(localVars, bounds);
    }

    return new SexpLocalVar(localVars.reserve(SexpLocalVar.SEXP_TYPE));
  }

  public VariableStrategy getStorage(LValue lhs) {
    return map.get(lhs);
  }
}

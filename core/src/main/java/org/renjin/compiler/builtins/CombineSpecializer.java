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

import org.renjin.compiler.ir.ArgumentBounds;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.primitives.combine.Combine;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.SEXP;

import java.util.List;

/**
 * Specializes calls to {@code c}
 */
public class CombineSpecializer implements Specializer {
  @Override
  public Specialization trySpecialize(RuntimeState runtimeState, List<ArgumentBounds> argumentTypes) {
    
    SEXP constantResult = tryCombine(argumentTypes);
    if(constantResult != null) {
      return new ConstantCall(constantResult);
    }
    
    return UnspecializedCall.INSTANCE;
  }

  private SEXP tryCombine(List<ArgumentBounds> argumentTypes) {
    ListVector.Builder constants = ListVector.newBuilder();
    for (ArgumentBounds argumentType : argumentTypes) {
      if(argumentType.getValueBounds().isConstant()) {
        constants.add(argumentType.getValueBounds().getConstantValue());
      } else {
        return null;
      }
    }

    return Combine.c(constants.build(), false);
    
  }

  private boolean allArgumentsAreAtomic(List<ValueBounds> argumentTypes) {
    for (ValueBounds argumentType : argumentTypes) {
      if((argumentType.getTypeSet() & ~TypeSet.ANY_ATOMIC_VECTOR) != 0) {
        return false;
      }
    }
    return true;
  }
  
  private boolean allAreConstant(List<ValueBounds> argumentBounds) {
    for (ValueBounds argumentBound : argumentBounds) {
      if(!argumentBound.isConstant()) {
        return false;
      }
    }
    return true;
  }


}

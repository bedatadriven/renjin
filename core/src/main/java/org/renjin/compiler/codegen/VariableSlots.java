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
package org.renjin.compiler.codegen;

import org.renjin.compiler.TypeSolver;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.expressions.LValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps variables to their slot indexes o
 */
public class VariableSlots {
  
  private final Map<LValue, VariableStorage> storage = new HashMap<>();
  private final int firstSlot;
  private int nextSlot = 0;
  
  public VariableSlots(int parameterSize, TypeSolver types) {

    firstSlot = parameterSize;
    
    for (Map.Entry<LValue, ValueBounds> entry : types.getVariables().entrySet()) {
      LValue variable = entry.getKey();
      ValueBounds bounds = entry.getValue();

      // If this variable is used, allocate storage for it.
      if(bounds != null) {
        storage.put(variable, new VariableStorage(firstSlot + nextSlot, bounds.storageType()));
        nextSlot += variable.getType().getSize();
      }
    }
  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder();
    for (Map.Entry<LValue, VariableStorage> entry : this.storage.entrySet()) {
      s.append(entry.getKey()).append(" => ").append(entry.getValue()).append("\n");
    }
    return s.toString();
  }

  public int getNumLocals() {
    return nextSlot;
  }

  public int getSlot(LValue lValue) {
    return storage.get(lValue).getSlotIndex();
  }

  public VariableStorage getStorage(LValue lhs) {
    return storage.get(lhs);
  }
  
}

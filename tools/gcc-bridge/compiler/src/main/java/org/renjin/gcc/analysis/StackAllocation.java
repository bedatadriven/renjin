/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${year} BeDataDriven Groep B.V. and contributors
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

package org.renjin.gcc.analysis;

import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleVarDecl;


public class StackAllocation extends Allocation {
  private final GimpleFunction function;
  private final GimpleVarDecl decl;

  public StackAllocation(GimpleFunction function, GimpleVarDecl decl) {
    this.function = function;
    this.decl = decl;
  }

  @Override
  public String toString() {
    return "stack{" + function.getMangledName() + "." + decl.getName() + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    StackAllocation that = (StackAllocation) o;

    return function.equals(that.function) && decl.equals(that.decl);

  }

  @Override
  public int hashCode() {
    int result = function.hashCode();
    result = 31 * result + decl.hashCode();
    return result;
  }
}

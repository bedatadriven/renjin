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
package org.renjin.gcc.analysis;

import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleVariableRef;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A set of local variables
 */
public class VarSet {

  private final Set<Long> ids = new HashSet<Long>();

  public VarSet() {
  }

  public VarSet(VarSet toCopy) {
    ids.addAll(toCopy.ids);
  }

  public void add(GimpleVariableRef ref) {
    ids.add(ref.getId());
  }

  public boolean contains(GimpleVariableRef ref) {
    return ids.contains(ref.getId());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    VarSet varSet = (VarSet) o;

    return ids.equals(varSet.ids);
  }

  @Override
  public int hashCode() {
    return ids.hashCode();
  }

  public static VarSet unionAll(Iterable<VarSet> inputs) {
    VarSet union = new VarSet();
    for (VarSet input : inputs) {
      union.ids.addAll(input.ids);
    }
    return union;
  }

  public boolean contains(GimpleExpr expr) {
    if(expr instanceof GimpleVariableRef) {
      return contains(((GimpleVariableRef) expr));
    } else {
      return false;
    }
  }

  public boolean contains(GimpleVarDecl varDecl) {
    return ids.contains(varDecl.getId());
  }

  public boolean isEmpty() {
    return ids.isEmpty();
  }

  @Override
  public String toString() {
    return ids.toString();
  }
}

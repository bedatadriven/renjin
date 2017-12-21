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
package org.renjin.gcc.symbols;

import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.gimple.expr.GimpleSymbolRef;
import org.renjin.repackaged.guava.base.Preconditions;
import org.renjin.repackaged.guava.collect.Maps;

import java.util.Map;

public class LocalVariableTable implements SymbolTable {

  private final UnitSymbolTable parent;
  private Map<Long, GExpr> variableMap = Maps.newHashMap();

  public LocalVariableTable(UnitSymbolTable parent) {
    this.parent = parent;
  }

  public boolean isRegistered(Long gimpleId) {
    return variableMap.containsKey(gimpleId);
  }

  public void addVariable(Long gimpleId, GExpr variable) {
    Preconditions.checkNotNull(variable);
    Preconditions.checkState(!variableMap.containsKey(gimpleId), "variable already registered with id " + gimpleId);

    variableMap.put(gimpleId, variable);
  }

  @Override
  public GExpr getVariable(GimpleSymbolRef ref) {
    GExpr variable = variableMap.get(ref.getId());
    if(variable == null) {
      if (parent == null) {
        throw new IllegalStateException("No variable with " + ref.getName() + " [id=" + ref.getId() + "]");
      } else {
        return parent.getVariable(ref);
      }
    }
    return variable;
  }

  public GExpr getVariable(GimpleVarDecl decl) {
    GExpr varGenerator = variableMap.get(decl.getId());
    if(varGenerator == null) {
      throw new IllegalStateException("No variable named " + decl.getName() + " [id=" + decl.getId() + "]");
    }
    return varGenerator;
  }

  public JExpr findHandle(GimpleFunctionRef functionRef) {
    return parent.findHandle(functionRef);
  }

  @Override
  public CallGenerator findCallGenerator(GimpleFunctionRef ref) {
    return parent.findCallGenerator(ref);
  }
}

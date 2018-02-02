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
package org.renjin.s4;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.primitives.packaging.Namespace;
import org.renjin.sexp.Environment;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import java.util.Optional;

public class S4ClassCache {

  private final Environment classTable;

  public S4ClassCache(Context context) {
    Optional<Namespace> methods = context.getNamespaceRegistry().getNamespaceIfPresent(Symbol.get("methods"));
    if(methods.isPresent()) {
      this.classTable = findClassTable(context, methods.get());
    } else {
      this.classTable = Environment.EMPTY;
    }
  }

  public Environment findClassTable(Context context, Namespace namespace) {
    SEXP classTableSexp = namespace.getNamespaceEnvironment().getVariableUnsafe(".classTable");
    if(classTableSexp == Symbol.UNBOUND_VALUE) {
      return Environment.EMPTY;
    }
    SEXP forcedClassTable = classTableSexp.force(context);
    if(forcedClassTable instanceof Environment) {
      return (Environment) forcedClassTable;
    } else {
      throw new EvalException("Expected '.classTable' to be an environment");
    }
  }

  public S4Class lookupClass(String name) {
    SEXP classRepresentation = classTable.getVariableUnsafe(name);
    if(classRepresentation == Symbol.UNBOUND_VALUE) {
      return null;
    } else {
      return new S4Class(classRepresentation);
    }
  }
}

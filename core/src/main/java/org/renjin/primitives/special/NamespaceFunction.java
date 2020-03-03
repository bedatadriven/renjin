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

package org.renjin.primitives.special;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.primitives.packaging.Namespace;
import org.renjin.sexp.*;

public class NamespaceFunction extends SpecialFunction {

  public NamespaceFunction(String name) {
    super(name);
  }

  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call) {
    checkArity(call, 2);
    Symbol namespaceSymbol = toSymbol(call.getArgument(0));
    Symbol entry = toSymbol(call.getArgument(1));

    Namespace namespace = context.getNamespaceRegistry().getNamespace(context, namespaceSymbol);

    if (getName().equals(":::")) {
      return namespace.getEntry(entry).force(context);
    } else {
      return namespace.getExport(entry).force(context);
    }
  }

  private Symbol toSymbol(SEXP sexp) {
    Symbol entry;
    if(sexp instanceof Symbol) {
      entry = (Symbol) sexp;
    } else if(sexp instanceof Vector && sexp.length() == 1) {
      entry = Symbol.get(((StringVector) sexp).getElementAsString(0));
    } else {
      throw new EvalException("cannot coerce type '" + sexp.getTypeName() + "' to vector of type 'character'");
    }
    return entry;
  }
}

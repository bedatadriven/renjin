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
package org.renjin.s4;

import org.renjin.eval.Context;
import org.renjin.primitives.packaging.Namespace;
import org.renjin.sexp.Frame;
import org.renjin.sexp.S4Object;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class S4ClassCache {

  private Map<String, S4Class> classTable = null;

  S4ClassCache() {
  }

  private void initializeCache(Context context) {

    classTable = new HashMap<>();

    List<Frame> loaded = new ArrayList<>();
    loaded.add(context.getGlobalEnvironment().getFrame());
    for(Namespace namespace : context.getNamespaceRegistry().getLoadedNamespaces()) {
      loaded.add(namespace.getNamespaceEnvironment().getFrame());
    }

    for(Frame frame : loaded) {
      for(Symbol symbol : frame.getSymbols()) {
        if(symbol.getPrintName().startsWith(S4.CLASS_PREFIX)) {
          String className = symbol.getPrintName().substring(S4.CLASS_PREFIX.length());
          S4Object classRepresentation = (S4Object) frame.getVariable(symbol).force(context);
          classTable.put(className, new S4Class(classRepresentation));
        }
      }
    }
  }

  /**
   *
   * @param context the current evaluation context
   * @param className the name of the class to lookup
   * @return the {@link S4Class} if defined, otherwise {@code null}.
   */
  public S4Class lookupClass(Context context, String className) {
    if(classTable == null) {
      initializeCache(context);
    }
    return classTable.get(className);
  }

  public boolean isSimple(String from, String to) {
    S4Class classDef = classTable.get(from);
    return classDef.isSimpleCoercion(to);
  }

  public SEXP coerceComplex(Context context, SEXP value, String from, String to) {
    S4Class classDef = classTable.get(from);
    return classDef.coerceTo(context, value, to);
  }
}

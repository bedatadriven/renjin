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
package org.renjin.eval;

import org.renjin.sexp.*;

import java.util.HashMap;
import java.util.Map;

public class DispatchChain {

  private String generic;
  private String group;
  private String method;
  private Closure closure;
  private Vector classes;

  private final Context context;

  public DispatchChain(Context context) {

    this.context = context;
  }

  public static DispatchChain newChain(Context context, Environment callingEnvironment, String generic, Vector classes) {
    for(int i = 0; i!=classes.length();++i) {
      Symbol method = Symbol.get(generic + "." + classes.getElementAsString(i));
      SEXP function = callingEnvironment.findVariable(context, method, (x -> x instanceof Function), true);
      if(function != Symbol.UNBOUND_VALUE) {
        DispatchChain chain = new DispatchChain(context);
        chain.classes = classes;
        chain.generic = generic;
        chain.method = method.getPrintName();
        chain.closure = (Closure) function;
        return chain;
      }
    }
    return null;
  }

  public Map<Symbol, SEXP> createMetadata() {
    Map<Symbol, SEXP> metadata = new HashMap<>();
    metadata.put(DispatchTable.CLASS, classes);
    metadata.put(DispatchTable.METHOD, new StringArrayVector(method));
    metadata.put(DispatchTable.GENERIC, StringVector.valueOf(generic));
    if(group != null) {
      metadata.put(DispatchTable.GROUP, StringVector.valueOf(group));
    }
    return metadata;
  }

  public Closure getClosure() {
    return closure;
  }

  public boolean next() {
    StringVector previous = (StringVector) classes;

    if(previous.length() <= 1) {
      classes = Null.INSTANCE;
      return false;
    } else {
      StringArrayVector.Builder newClass = StringVector.newBuilder();
      for(int i=1; i!=previous.length();++i) {
        newClass.add(previous.getElementAsString(i));
      }
      newClass.setAttribute(Symbol.get("previous"), previous);
      this.classes = newClass.build();

    }
    return false;
  }

  public Symbol getMethodSymbol() {
    return Symbol.get(method);
  }
}

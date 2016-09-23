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
package org.renjin.eval;

import org.renjin.primitives.CollectionUtils;
import org.renjin.sexp.*;

public class DispatchChain {
  public static final Symbol GENERIC = Symbol.get(".Generic");
  public static final Symbol METHOD = Symbol.get(".Method");
  public static final Symbol CLASS = Symbol.get(".Class");
  public static final Symbol GROUP = Symbol.get(".Group");

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
      SEXP function = callingEnvironment.findVariable(context, method, CollectionUtils.IS_FUNCTION, true);
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

  public void populateEnvironment(Environment rho) {
    rho.setVariable(CLASS, classes);
    rho.setVariable(METHOD, new StringArrayVector(method));
    rho.setVariable(GENERIC, StringVector.valueOf(generic));
    if(group != null) {
      rho.setVariable(GROUP, StringVector.valueOf(group));
    }
  }

  public Closure getClosure() {
    return closure;
  }

  /**
   * Processes the {@code object} argument (for Use/NextMethod).
   * Can override the {@code .Generic} in the environment
   * @param sexp
   */
  public DispatchChain withGenericArgument(SEXP sexp) {
    if(sexp instanceof StringVector) {
      generic = ((StringVector) sexp).getElementAsString(0);
    }
    return this;
  }

  public DispatchChain withObjectArgument(SEXP sexp) {
    if(sexp != Null.INSTANCE) {
      throw new EvalException("oops, NextMethod(object!=null) is not yet implemented");
    }
    return this;
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

  public Symbol getGenericSymbol() {
    return Symbol.get(generic);
  }

  public Symbol getMethodSymbol() {
    return Symbol.get(method);
  }
}

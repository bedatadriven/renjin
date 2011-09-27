/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package r.base.dispatch;

import r.lang.*;
import r.lang.exception.EvalException;

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


  public DispatchChain() {

  }

  public static DispatchChain newChain(Environment callingEnvironment, String generic, Vector classes) {
    for(int i = 0; i!=classes.length();++i) {
      Symbol method = Symbol.get(generic + "." + classes.getElementAsString(i));
      SEXP function = callingEnvironment.findVariable(method, CollectionUtils.IS_FUNCTION, true);
      if(function != Symbol.UNBOUND_VALUE) {
        DispatchChain chain = new DispatchChain();
        chain.classes = classes;
        chain.generic = generic;
        chain.method = method.getPrintName();
        chain.closure = (Closure) function;
        return chain;
      }
    }
    return null;
  }

  public static DispatchChain fromEnvironment(Environment rho) {
    DispatchChain chain = new DispatchChain();
    chain.classes = (Vector) rho.getVariable(CLASS);
    chain.method = ((Vector)rho.getVariable(METHOD)).getElementAsString(0);
    chain.generic = ((Vector)rho.getVariable(GENERIC)).getElementAsString(0);
    if(rho.hasVariable(GROUP)) {
      chain.group = ((Vector)rho.getVariable(GROUP)).getElementAsString(0);
    }
    return chain;
  }

  public void populateEnvironment(Environment rho) {
    rho.setVariable(CLASS, classes);
    rho.setVariable(METHOD, new StringVector(method));
    rho.setVariable(GENERIC, new StringVector(generic));
    if(group != null) {
      rho.setVariable(GROUP, new StringVector(group));
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
      StringVector.Builder newClass = StringVector.newBuilder();
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

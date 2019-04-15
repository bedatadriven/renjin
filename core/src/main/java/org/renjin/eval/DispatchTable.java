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

package org.renjin.eval;

import org.renjin.sexp.Environment;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbol;

public class DispatchTable {

  public static final Symbol GENERIC = Symbol.get(".Generic");
  public static final Symbol METHOD = Symbol.get(".Method");
  public static final Symbol CLASS = Symbol.get(".Class");
  public static final Symbol GROUP = Symbol.get(".Group");
  /**
   * ‘.Generic’ is a length-one character vector naming the generic function.
   */
  private String generic;

  private String group;
  /**
   * ‘.Class’ is a character vector of classes used to find the next
   * method.  ‘NextMethod’ adds an attribute ‘"previous"’ to ‘.Class’
   * giving the ‘.Class’ last used for dispatch, and shifts ‘.Class’
   * along to that used for dispatch.
   */
  public StringVector classVector;

  /**
   * ‘.Method’ is a character vector (normally of length one) naming
   * the method function.  (For functions in the group generic ‘Ops’ it
   * is of length two.)
   */
  public String method;

  private Environment genericDefinitionEnvironment;

  /**
   * ‘.GenericCallEnv’ is the environment of the
   * call to be generic.
   */
  private Environment genericCallEnvironment;


  public DispatchTable(Environment definitionEnvironment, String generic, String group) {
    this.genericDefinitionEnvironment = definitionEnvironment;
    this.generic = generic;
    this.group = group;
  }

  public DispatchTable(Environment definitionEnvironment, String genericName, StringVector classes) {
    this.genericDefinitionEnvironment = definitionEnvironment;
    this.generic = genericName;
    this.classVector = classes;
  }

  public SEXP getMethodSymbol() {
    return Symbol.get(method);
  }

  public SEXP get(Symbol symbol) {
    if(symbol == GENERIC) {
      return StringVector.valueOf(generic);
    }
    if(symbol == METHOD) {
      throw new UnsupportedOperationException("TODO");
    }
    if(symbol == GROUP) {
      throw new UnsupportedOperationException("TODO");
    }
    if(symbol == CLASS) {
      return classVector;
    }
    return null;
  }
}

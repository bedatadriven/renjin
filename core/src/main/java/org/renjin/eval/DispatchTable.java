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

import org.renjin.sexp.*;

import java.util.Arrays;
import java.util.Collection;

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
   * The name of the method that was selected during dispatch. For example,
   * "print.foo" or "Ops.factor"
   */
  public String method;

  /**
   * For dispatch on the Ops group, we also need to store the method selected
   * for the second argument. This will be either the same as {@link #method} or ""
   */
  public String method2;


  private Environment genericDefinitionEnvironment;

  /**
   * ‘.GenericCallEnv’ is the environment of the
   * call to be generic.
   */
  private Environment genericCallEnvironment;

  /**
   * The original argument to the matched function.
   *
   * <p>This value is stored when apply the closure and is used
   * by NextMethod.</p>
   */
  public MatchedArguments arguments;



  public DispatchTable(Environment definitionEnvironment, String generic) {
    this.genericDefinitionEnvironment = definitionEnvironment;
    this.generic = generic;
  }

  public DispatchTable(Environment definitionEnvironment, String genericName, StringVector classes) {
    this.genericDefinitionEnvironment = definitionEnvironment;
    this.generic = genericName;
    this.classVector = classes;
  }

  public String getGeneric() {
    return generic;
  }

  public String getGroup() {
    return group;
  }

  public Environment getGenericDefinitionEnvironment() {
    return genericDefinitionEnvironment;
  }

  public SEXP getMethodSymbol() {
    return Symbol.get(method);
  }

  public SEXP get(Symbol symbol) {
    if(symbol == GENERIC) {
      return StringVector.valueOf(generic);
    }
    if(symbol == METHOD) {
      if(method2 == null) {
        return StringVector.valueOf(method);
      } else {
        return new StringArrayVector(method, method2);
      }
    }
    if(symbol == GROUP) {
      if(group == null) {
        return StringVector.valueOf("");
      } else {
        return StringVector.valueOf(group);
      }
    }
    if(symbol == CLASS) {
      if(classVector == null) {
        return Null.INSTANCE;
      } else {
        return classVector;
      }
    }
    return null;
  }

  public Collection<Symbol> getEnvironmentSymbols() {
    return Arrays.asList(GENERIC, METHOD, GROUP, CLASS);
  }

  public StringVector getClassVector() {
    return classVector;
  }

}

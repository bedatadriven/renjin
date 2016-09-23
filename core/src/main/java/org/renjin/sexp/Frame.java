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
package org.renjin.sexp;

import org.renjin.eval.Context;

import java.util.Set;

/**
 * Defines the interface to objects providing the storage for 
 * {@link Environment}s
 */
public interface Frame {
  /**
   * 
   * @return the names of all the objects stored in this frame
   */
  Set<Symbol> getSymbols();
  
  /**
   * 
   * @param name the name of the variable to lookup
   * @return the value of the variable named {@code name}, or {@code Symbol.UNBOUND} if 
   * no such variable is bound to this frame.
   */
  SEXP getVariable(Symbol name);
  
  /**
   * Retrieves a function value from the frame. This does basically the same as 
   * {@code getVariable()} but will only return function values, allowing the implementation
   * to optimize this frequent case.
   * 
   *
   * @param context
   * @param name the name of the variable to lookup
   * @return the function value of the variable bound to this frame, or {@code Symbol.UNBOUND} if
   * no such variable exists
   */
  Function getFunction(Context context, Symbol name);
  
  /**
   * 
   * @param name
   * @return true if {@code MISSING_ARGUMENT} is bound to this symbol
   */
  boolean isMissingArgument(Symbol name);
  
  /**
   * 
   * @param name 
   * @param value
   */
  void setVariable(Symbol name, SEXP value);
  
  /**
   * Removes all values bound to this variable.
   */
  void clear();

  void remove(Symbol name);
  
}

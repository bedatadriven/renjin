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

package org.renjin.sexp;

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
   * @param name the name of the variable to lookup
   * @return the function value of the variable bound to this frame, or {@code Symbol.UNBOUND} if 
   * no such variable exists
   */
  Function getFunction(Symbol name);
  
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

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

package org.renjin.sexp;

import org.renjin.eval.Context;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation for a frame for a compiled function.
 *
 * <p>In many cases, we can't store R function variables as JVM local variables because
 * they need to be visible via the function's environment.</p>
 *
 * <p>However, we can still scrape some efficiency savings by allocating a fixed array of
 * values for the variables known at compile time to be referenced from within the function's
 * compiled code.</p>
 *
 * <p>At compile time, we assign each variable a fixed index within the frame array. All compiled code
 * can then refer to the variable directly by it's array index and avoid a lookup in the hash map.</p>
 *
 * <p>Should code elsewhere access the variables, we can still first lookup the index by name and return
 * the correct value from the frame array. For variables defined at runtime not anticipated by the compiler,
 * we have an overflow HashMap that is allocated on demand.</p>
 */
public class CompiledFrame implements Frame {

  private final SEXP[] names;
  private final SEXP[] arguments;
  private final SEXP[] frame;
  private HashMap<Symbol, SEXP> overflow = null;

  /**
   * @param names a {@link ListVector} containing the names of the variables in this local frame
   * @param arguments the original arguments array passed to the function.
   * @param locals an array the same length as the names array used to store local variables.
   */
  CompiledFrame(SEXP[] names, SEXP[] arguments, SEXP[] locals) {
    this.names = names;
    this.arguments = arguments;
    this.frame = locals;
  }

  private int indexOf(Symbol name) {
    for (int i = 0; i < frame.length; i++) {
      if (names[i] == name) {
        return i;
      }
    }
    return -1;
  }

  public SEXP get(int localIndex) {
    SEXP value = frame[localIndex];
    if(value == null) {
      return Symbol.UNBOUND_VALUE;
    } else {
      return value;
    }
  }

  public void set(int localIndex, SEXP value) {
    frame[localIndex] = value;
  }

  @Override
  public Set<Symbol> getSymbols() {
    Set<Symbol> set = new HashSet<>();
    for (int i = 0; i < names.length; i++) {
      set.add((Symbol)names[i]);
    }
    if(overflow != null) {
      set.addAll(overflow.keySet());
    }
    return set;
  }

  @Override
  public SEXP getVariable(Symbol name) {
    int index = indexOf(name);
    SEXP value = null;
    if(index != -1) {
      // Stored in the primary frame array
      value = frame[index];

    } else {
      // Stored in the overflow hashmap
      if(overflow != null) {
        value = overflow.get(name);
      }
    }
    if(value == null) {
      return Symbol.UNBOUND_VALUE;
    } else {
      return value;
    }
  }

  @Override
  public Function getFunction(Context context, Symbol name) {
    // TODO:
    return null;
  }

  @Override
  public boolean isMissingArgument(Symbol name) {
    int argumentCount = arguments.length;
    for (int i = 0; i < argumentCount; i++) {
      if (names[i] == name && arguments[i] == null) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void setVariable(Symbol name, SEXP value) {
    int index = indexOf(name);
    if(index == -1) {
      if(overflow == null) {
        overflow = new HashMap<>();
      }
      overflow.put(name, value);
    } else {
      frame[index] = value;
    }
  }

  @Override
  public void clear() {
    Arrays.fill(frame, null);
    overflow = null;
  }

  @Override
  public void remove(Symbol name) {
    int index = indexOf(name);
    if(index == -1) {
      if(overflow != null) {
        overflow.remove(name);
      }
    } else {
      frame[index] = null;
    }
  }

}

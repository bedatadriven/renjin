/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
import org.renjin.eval.EvalException;

import java.util.IdentityHashMap;
import java.util.Map.Entry;
import java.util.Set;


public class HashFrame implements Frame{

  private IdentityHashMap<Symbol, SEXP> values = new IdentityHashMap<Symbol, SEXP>();
  
  /**
   * Bloom-esque filter keeping track of which functions have 
   * been (potentially) set into this frame. 
   */
  private int functionFilter = 0;
  
  @Override
  public Set<Symbol> getSymbols() {
    return values.keySet();
  }

  @Override
  public SEXP getVariable(Symbol name) {
    SEXP value = values.get(name);
    return value == null ? Symbol.UNBOUND_VALUE : value;
  }

  @Override
  public Function getFunction(Context context, Symbol name) {
    if(functionFilter != 0 && (functionFilter & name.hashBit()) != 0) {
      SEXP value = values.get(name);
      if(value != null) {
        value = value.force(context);
        if(value == Symbol.MISSING_ARG) {
          throw new EvalException("argument '%s' is missing with no default", name.toString());
        }
        if(value instanceof Function) {
          return (Function)value;
        }
      }
    }
    return null;
  }


  @Override
  public boolean isMissingArgument(Symbol name) {
    if(functionFilter != 0 && (functionFilter & name.hashBit()) != 0) {
      return values.get(name) == Symbol.MISSING_ARG;
    }
    return false;
  }
  
  @Override
  public void setVariable(Symbol name, SEXP value) {
    values.put(name, value);
    // we add Promises to the function filter because they *could* be 
    // functions
    if(value instanceof Function || value instanceof Promise ||
        value == Symbol.MISSING_ARG) {
      functionFilter |= name.hashBit();
    }
  }

  @Override
  public void remove(Symbol name) {
    values.remove(name);
  }

  @Override
  public void clear() {
    values.clear();
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for(Entry<Symbol,SEXP> entry : values.entrySet()) {
      sb.append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
    }
    return sb.toString();
  }

}

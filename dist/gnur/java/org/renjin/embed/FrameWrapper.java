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
package org.renjin.embed;

import org.renjin.eval.Context;
import org.renjin.repackaged.guava.collect.Sets;
import org.renjin.sexp.Frame;
import org.renjin.sexp.Function;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;
import org.rosuda.JRI.Rengine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A frame linking the GNU R Global environment
 *  to the Renjin Global environment
 */
public class FrameWrapper implements Frame {

  private final Rengine rengine;
  private final long environmentPointer;

  /**
   * Pointer to GNU R's unbound value symbol
   */
  private final long unboundValuePointer;
  private Wrapper wrapper;

  private Set<Symbol> symbols = null;

  private Map<Symbol, SEXP> cache = new HashMap<>();
  private Map<Symbol, SEXP> pendingWrites = new HashMap<>();
  private Map<Symbol, SEXP> local = new HashMap<>();

  public FrameWrapper(Rengine rengine, long environmentPointer) {
    this.rengine = rengine;
    this.environmentPointer = environmentPointer;
    this.unboundValuePointer = rengine.rniSpecialObject(Rengine.SO_UnboundValue);
  }

  public void setWrapper(Wrapper wrapper) {
    this.wrapper = wrapper;
  }

  public void clearCache() {
    symbols = null;
  }

  private void fetchSymbols() {
    if(symbols == null) {
      symbols = new HashSet<>();
      symbols.addAll(local.keySet());

      long list = rengine.rniListEnv(environmentPointer, true);
      if(list != 0) {
        String[] strings = rengine.rniGetStringArray(list);
        if(strings != null) {
          for (String string : strings) {
            symbols.add(Symbol.get(string));
          }
        }
      }
    }
  }

  @Override
  public Set<Symbol> getSymbols() {
    fetchSymbols();
    return Sets.union(symbols, pendingWrites.keySet());
  }

  @Override
  public SEXP getVariable(Symbol name) {

    SEXP cachedValue = cache.get(name);
    if(cachedValue != null) {
      return cachedValue;
    }

    fetchSymbols();

    SEXP value = Symbol.UNBOUND_VALUE;
    if(symbols.contains(name)) {
      long result = rengine.rniFindVar(name.getPrintName(), environmentPointer);
      if(result == unboundValuePointer) {
        value = Symbol.UNBOUND_VALUE;
      } else {
        value = wrapper.wrap(result);
      }
      cache.put(name, value);
    }
    return value;
  }

  @Override
  public Function getFunction(Context context, Symbol name) {
    SEXP value = getVariable(name);
    if(value instanceof Function) {
      return (Function) value;
    }
    return null;
  }

  @Override
  public boolean isMissingArgument(Symbol name) {
    return false;
  }

  @Override
  public void setVariable(Symbol name, SEXP value) {
    if(wrapper == null) {
      // We are not yet live, so keep this as a "local" to Renjin
      local.put(name, value);
      cache.put(name, value);

    } else {
      pendingWrites.put(name, value);
      cache.put(name, value);
    }
  }

  @Override
  public void clear() {
    fetchSymbols();
    for (Symbol symbol : symbols) {
      remove(symbol);
    }
  }

  @Override
  public void remove(Symbol name) {
    if(symbols != null) {
      symbols.remove(name);
    }
    pendingWrites.put(name, Symbol.UNBOUND_VALUE);
    cache.remove(name);
  }
}

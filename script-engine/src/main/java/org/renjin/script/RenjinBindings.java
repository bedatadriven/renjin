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
package org.renjin.script;

import org.renjin.eval.EvalException;
import org.renjin.invoke.reflection.converters.Converters;
import org.renjin.repackaged.guava.collect.Sets;
import org.renjin.sexp.Environment;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import javax.script.Bindings;
import java.util.*;


public class RenjinBindings implements Bindings {

  private final Environment environment;
  
  public RenjinBindings(Environment environment) {
    super();
    this.environment = environment;
  }

  @Override
  public void clear() {
    ArrayList<Symbol> names = new ArrayList<>(environment.getSymbolNames());
    for (Symbol name : names) {
      names.remove(name);
    }
  }

  @Override
  public boolean containsValue(Object value) {
    for(Symbol symbol : environment.getSymbolNames()) {
      if(!environment.isActiveBinding(symbol) &&
          environment.getVariableOrThrowIfActivelyBound(symbol).equals(value)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Set<java.util.Map.Entry<String, Object>> entrySet() {
    throw new UnsupportedOperationException("nyi");
  }

  @Override
  public boolean isEmpty() {
    return environment.getSymbolNames().isEmpty();
  }

  @Override
  public Set<String> keySet() {
    Set<String> names = Sets.newHashSet();
    for(Symbol symbol : environment.getSymbolNames()) {
      names.add(symbol.getPrintName());
    }
    return names;
  }

  @Override
  public int size() {
    return environment.length();
  }

  @Override
  public Collection<Object> values() {
    List<Object> values = new ArrayList<>();
    for (Symbol symbolName : environment.getSymbolNames()) {
      if(!environment.isActiveBinding(symbolName)) {
        values.add(environment.getVariableOrThrowIfActivelyBound(symbolName));
      }
    }
    return values;
  }

  @Override
  public boolean containsKey(Object key) {
    return environment.exists(toSymbol(key));
  }

  @Override
  public Object get(Object key) {
    Symbol symbol = toSymbol(key);
    if(environment.exists(symbol) && !environment.isActiveBinding(symbol)) {
      return environment.getVariableOrThrowIfActivelyBound(symbol);
    }
    return null;
  }
  
  private Symbol toSymbol(Object key) {
    if(key instanceof Symbol) {
      return (Symbol)key;
    } else if(key instanceof String) {
      return Symbol.get((String)key);
    } else {
      return Symbol.UNBOUND_VALUE;
    }
  }

  @Override
  public Object put(String name, Object value) {
    Symbol symbol = Symbol.get(name);
    if(environment.isActiveBinding(symbol)) {
      throw new EvalException("Symbol " + name + " is actively bound.");
    }
    SEXP previousValue = environment.getVariableOrThrowIfActivelyBound(symbol);
    SEXP exp;
    if(value == null) {
      exp = Null.INSTANCE;
    } else {
      exp = Converters.get(value.getClass()).convertToR(value);
    }
    environment.setVariableUnsafe(symbol, exp);
    return previousValue;
  }

  @Override
  public void putAll(Map<? extends String, ? extends Object> toMerge) {
    for(Map.Entry<? extends String, ? extends Object> entry : toMerge.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public Object remove(Object key) {
    Object originalValue = get(key);
    environment.remove(toSymbol(key));
    return originalValue;
  }
}
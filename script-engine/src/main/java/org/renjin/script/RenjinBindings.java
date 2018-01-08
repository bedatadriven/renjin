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
package org.renjin.script;

import org.renjin.invoke.reflection.converters.Converters;
import org.renjin.repackaged.guava.base.Function;
import org.renjin.repackaged.guava.collect.Collections2;
import org.renjin.repackaged.guava.collect.Sets;
import org.renjin.sexp.Frame;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import javax.script.Bindings;
import java.util.Collection;
import java.util.Map;
import java.util.Set;


public class RenjinBindings implements Bindings {

  private final Frame frame;
  
  public RenjinBindings(Frame frame) {
    super();
    this.frame = frame;
  }

  @Override
  public void clear() {
    frame.clear();
  }

  @Override
  public boolean containsValue(Object value) {
    for(Symbol symbol : frame.getSymbols()) {
      if(frame.getVariable(symbol).equals(value)) {
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
    return frame.getSymbols().isEmpty();
  }

  @Override
  public Set<String> keySet() {
    Set<String> names = Sets.newHashSet();
    for(Symbol symbol : frame.getSymbols()) {
      names.add(symbol.getPrintName());
    }
    return names;
  }

  @Override
  public int size() {
    return frame.getSymbols().size();
  }

  @Override
  public Collection<Object> values() {
    return Collections2.transform(frame.getSymbols(), new Function<Symbol, Object>() {

      @Override
      public Object apply(Symbol symbol) {
        return frame.getVariable(symbol);
      }
    });
  }

  @Override
  public boolean containsKey(Object key) {
    return frame.getSymbols().contains(toSymbol(key));
  }

  @Override
  public Object get(Object key) {
    return frame.getVariable(toSymbol(key));
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
    SEXP previousValue = frame.getVariable(symbol);
    SEXP exp;
    if(value == null) {
      exp = Null.INSTANCE;
    } else {
      exp = Converters.get(value.getClass()).convertToR(value);
    }
    frame.setVariable(symbol, exp);
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
    frame.remove(toSymbol(key));
    return originalValue;
  }
}
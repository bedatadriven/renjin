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
package org.renjin.eval;

import org.renjin.repackaged.guava.collect.Maps;
import org.renjin.sexp.*;

import java.util.Map;
import java.util.Set;

/**
 * Session-level options for 
 * @author Alex
 *
 */
public class Options {
  private Map<String, SEXP> map;

  public Options() {
    map = Maps.newHashMap();
    map.put("prompt", new StringArrayVector("> "));
    map.put("continue", new StringArrayVector("+ "));
    map.put("expressions" , new IntArrayVector(5000));
    map.put("width", new IntArrayVector(80));
    map.put("digits", new IntArrayVector(7));
    map.put("echo", new LogicalArrayVector(false));
    map.put("verbose", new LogicalArrayVector(false));
    map.put("check.bounds", new LogicalArrayVector(false));
    map.put("keep.source", new LogicalArrayVector(true));
    map.put("keep.source.pkgs", new LogicalArrayVector(false));
    map.put("warnings.length", new IntArrayVector(1000));
    map.put("OutDec", new StringArrayVector("."));
    map.put("encoding", new StringArrayVector("UTF8"));
  }

  public SEXP get(String name) {
    SEXP value = map.get(name);
    return value == null ? Null.INSTANCE : value;
  }
  
  public int getInt(String name, int defaultValue) {
    SEXP value = get(name);
    if(value instanceof AtomicVector && value.length() >= 1) {
      return ((AtomicVector)value).getElementAsInt(0);
    }
    return defaultValue;
  }

  public SEXP set(String name, SEXP value) {
    SEXP old = map.put(name, value);
    return old == null ? Null.INSTANCE : value;
  }

  public Set<String> names() {
    return map.keySet();
  }

}
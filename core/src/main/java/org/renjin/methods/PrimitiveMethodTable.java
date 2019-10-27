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
package org.renjin.methods;

import org.renjin.repackaged.guava.cache.CacheBuilder;
import org.renjin.repackaged.guava.cache.CacheLoader;
import org.renjin.repackaged.guava.cache.LoadingCache;
import org.renjin.sexp.Closure;
import org.renjin.sexp.Function;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;

import java.util.concurrent.ExecutionException;

public class PrimitiveMethodTable {

  public enum prim_methods_t {NO_METHODS, NEEDS_RESET, HAS_METHODS, SUPPRESSED} ;

  public static class Entry {

    private prim_methods_t methods;
    private Closure generic;
    private SEXP methodList = Null.INSTANCE;
    
    public void setMethods(prim_methods_t code) {
      this.methods = code;
    }

    
    public void setGeneric(Closure fundef) {
      this.generic = fundef;
    }

    public void setMethodList(SEXP methodList) {
      this.methodList = methodList;
    }
    
  }

  private LoadingCache<Function, Entry> map;
  private boolean primitiveMethodsAllowed = false;
  
  public PrimitiveMethodTable() {
    this.map = CacheBuilder.newBuilder()
        .build(new CacheLoader<Function, Entry>() {

          @Override
          public Entry load(Function key) throws Exception {
            return new Entry();
          }
        });
  }
      
  public Entry get(Function fn) {
    try {
      return map.get(fn);
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  public boolean isPrimitiveMethodsAllowed() {
    return primitiveMethodsAllowed;
  }

  public void setPrimitiveMethodsAllowed(boolean primitiveMethodsAllowed) {
    this.primitiveMethodsAllowed = primitiveMethodsAllowed;
  }
 
}

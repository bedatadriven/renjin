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
package org.renjin.compiler.builtins;

import org.renjin.compiler.ir.exception.InternalCompilerException;
import org.renjin.primitives.Primitives;
import org.renjin.repackaged.guava.base.Preconditions;
import org.renjin.repackaged.guava.cache.CacheBuilder;
import org.renjin.repackaged.guava.cache.CacheLoader;
import org.renjin.repackaged.guava.cache.LoadingCache;
import org.renjin.repackaged.guava.collect.Maps;

import java.util.Map;
import java.util.concurrent.ExecutionException;


public class BuiltinSpecializers {

  public static final BuiltinSpecializers INSTANCE = new BuiltinSpecializers();


  /**
   * For a few builtins, we have extra-special specializers.
   */
  private final Map<String, Specializer> specializers = Maps.newHashMap();

  
  /**
   * For most builtins, specialization is done automatically using @Annotations present
   * in the code base. To avoid rebuilding the metadata each time one is needed, 
   * cache instances of BuiltinSpecializer here.
   */
  private final LoadingCache<String, BuiltinSpecializer> cache;
  
  
  public BuiltinSpecializers() {
    
    specializers.put("length", new GenericBuiltinGuard(new LengthSpecializer()));
    specializers.put("[<-", new GenericBuiltinGuard(new ReplaceSpecializer()));
    specializers.put("[", new GenericBuiltinGuard(new SubsetSpecializer()));
    specializers.put("c", new GenericBuiltinGuard(new CombineSpecializer()));
    specializers.put("is.array", new GenericBuiltinGuard(new IsArraySpecializer()));
    specializers.put("dim", new GenericBuiltinGuard(new DimSpecializer()));

    cache = CacheBuilder.newBuilder().build(new CacheLoader<String, BuiltinSpecializer>() {
      @Override
      public BuiltinSpecializer load(String primitiveName) throws Exception {
        Primitives.Entry builtinEntry = Primitives.getBuiltinEntry(primitiveName);
        if(builtinEntry == null) {
          throw new IllegalStateException("No builtin entry for " + primitiveName);
        }
        return new BuiltinSpecializer(builtinEntry);
      }
    });
  }
  
  public Specializer get(String primitiveName) {
    
    if(specializers.containsKey(primitiveName)) {
      return specializers.get(primitiveName);
    }
    
    try {
      return cache.get(primitiveName);
    } catch (ExecutionException e) {
      throw new InternalCompilerException(e);
    }
  }
}

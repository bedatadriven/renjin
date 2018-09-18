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
package org.renjin.compiler.builtins;

import org.renjin.compiler.NotCompilableException;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.exception.InternalCompilerException;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.primitives.Primitives;
import org.renjin.repackaged.guava.cache.CacheBuilder;
import org.renjin.repackaged.guava.cache.CacheLoader;
import org.renjin.repackaged.guava.cache.LoadingCache;
import org.renjin.repackaged.guava.collect.Maps;
import org.renjin.sexp.Symbol;

import java.util.List;
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
  private final LoadingCache<String, Specializer> cache;
  
  
  public BuiltinSpecializers() {
    
    specializers.put("length", new GenericBuiltinGuard(new LengthSpecializer()));
    specializers.put("[<-", new GenericBuiltinGuard(new ReplaceSpecializer()));
    specializers.put("[", new GenericBuiltinGuard(new SubsetSpecializer()));
    specializers.put("[[", new GenericBuiltinGuard(new SingleSubsetSpecializer()));
    specializers.put(".subset2", new SingleSubsetSpecializer());
    specializers.put("$", new GenericBuiltinGuard(new DollarSpecializer()));
    specializers.put("c", new GenericBuiltinGuard(new CombineSpecializer()));
    specializers.put("is.array", new GenericBuiltinGuard(new IsArraySpecializer()));
    specializers.put("attributes", new GetAttributesSpecializer());
    specializers.put("attributes<-", new SetAttributesSpecializer());
    specializers.put("dim", new GenericBuiltinGuard(new DimSpecializer()));
    specializers.put("rep", new RepSpecializer());
    specializers.put("sum", new GenericBuiltinGuard(new SumSpecializer()));
    specializers.put("invisible", new InvisibleSpecializer());
    specializers.put("as.logical", new GenericBuiltinGuard(new AsVectorSpecializer("as.logical", TypeSet.LOGICAL)));
    specializers.put("nargs", new NargsSpecializer());
    specializers.put("cbind", new BindSpecializer("cbind"));
    specializers.put("rbind", new BindSpecializer("rbind"));
    specializers.put("list", new ListSpecializer());
    specializers.put("stopifnot", new StopIfNotSpecializer());
    specializers.put("inherits", new InheritsSpecializer());
    specializers.put("is.numeric", new IsNumericSpecializer());
    specializers.put("range", new GenericBuiltinGuard(new RangeSpecializer()));

    cache = CacheBuilder.newBuilder().build(new CacheLoader<String, Specializer>() {
      @Override
      public Specializer load(String primitive) throws Exception {
        Symbol primitiveName = Symbol.get(primitive);
        Primitives.Entry entry = Primitives.getBuiltinEntry(primitiveName);
        if(entry == null) {
          entry = Primitives.getInternalEntry(primitiveName);
        }
        if(entry == null) {
          return new Specializer() {
            @Override
            public Specialization trySpecialize(RuntimeState runtimeState, List<ArgumentBounds> arguments) {
              return new Specialization() {
                @Override
                public ValueBounds getResultBounds() {
                  return ValueBounds.UNBOUNDED;
                }

                @Override
                public boolean isPure() {
                  return false;
                }

                @Override
                public CompiledSexp getCompiledExpr(EmitContext emitContext) {
                  throw new NotCompilableException(primitiveName);
                }
              };
            }
          };
        }
        AnnotationBasedSpecializer specializer = new AnnotationBasedSpecializer(entry);
        if(specializer.isGeneric()) {
          return new GenericBuiltinGuard(specializer);
        } else {
          return specializer;
        }
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
